package com.donotfinishlast.webgame.controller

import com.donotfinishlast.webgame.constants.Constants
import com.donotfinishlast.webgame.dto.RoomDto
import com.donotfinishlast.webgame.entity.Room
import com.donotfinishlast.webgame.service.Action
import com.donotfinishlast.webgame.service.MessageService
import com.donotfinishlast.webgame.service.RoomService
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.lang.RuntimeException
import java.lang.Exception


@Controller
class MyWebSocketHandler : TextWebSocketHandler() {

    @Autowired
    private lateinit var roomService: RoomService

    @Autowired
    private lateinit var messageService: MessageService

    @Autowired
    private lateinit var modelMapper:ModelMapper

    private val roomsAndSessions: MutableMap<String,List<WebSocketSession>> = mutableMapOf()

    private val gson = Gson()
    private val mapType = object : TypeToken<Map<String, Any>>() {}.type

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val roomId = session.attributes["roomId"] as String
        val room = roomService.getRoomDto(roomId)
        val sessions = roomsAndSessions[roomId]?.toMutableList() ?: mutableListOf()
        for (webSocketSession in sessions) {
            if (!webSocketSession.isOpen) {
                sessions.remove(webSocketSession)
            }
        }
        val sessionIds = sessions.map { it.id }
        val player = session.attributes["playerId"] as String
        if(sessionIds.contains(session.id)){
            session.sendMessage(TextMessage(messageService.getMessage(player,Action.WAIT,room)))
            return
        } else if (sessionIds.size >= 2){
            session.sendMessage(TextMessage(messageService.getMessage(player,Action.FULL,room)))
            return
        } else{
            sessions.add(session)
            if(sessions.size == 1){
                session.sendMessage(TextMessage(messageService.getMessage(player,Action.WAIT,room)))
            } else {
                val randomizedSessions = sessions.shuffled()
                val player1 = randomizedSessions[0].attributes["playerId"] as String
                val player2 = randomizedSessions[1].attributes["playerId"] as String
                randomizedSessions[0].sendMessage(TextMessage(messageService.getMessage(player1,Action.PLAY,room)))
                randomizedSessions[1].sendMessage(TextMessage(messageService.getMessage(player2,Action.PAUSE,room)))
                }
            }
            roomsAndSessions[roomId] = sessions
        }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // Handle incoming messages
        try {
            val payload = message.payload
            val roomId = session.attributes["roomId"] as String
            val playerId = session.attributes["playerId"] as String
            val room = roomService.getRoom(roomId)

            // parse data from payload
            val resultMap: Map<String, Any> = gson.fromJson(payload, mapType)
            val actionReceived = resultMap["action"]
            val playerMarkedSegment = resultMap["segment"]?.toString()?.toDouble()?.toInt()
            val playerMarkedPositions = (resultMap["selections"] as List<String>?)?.map { it.toInt() }

            if (actionReceived != Action.SUBMIT.name && actionReceived != Action.SELECT.name) {
                throw RuntimeException("invalid Action from $playerId")
            }

            if (playerMarkedSegment == null || playerMarkedPositions.isNullOrEmpty()) {
                throw RuntimeException("invalid input $playerId")
            }

            val opponentSession = getOpponentSession(roomId, session)

            if (actionReceived == Action.SUBMIT.name) {
                val boardUpdatedRoom =
                    updateUserMarkedPositions(playerId, playerMarkedSegment, playerMarkedPositions, room)
                val updatedRoom = roomService.saveRoom(boardUpdatedRoom)
                sendPlayPauseActions(updatedRoom, session, opponentSession)
            } else if (actionReceived == Action.SELECT.name) {
                val lockedPlayer = getLockedPlayerId(playerId)
                val boardUpdatedRoom =
                    updateUserMarkedPositions(lockedPlayer, playerMarkedSegment, playerMarkedPositions, room)
                val updatedRoom = modelMapper.map(boardUpdatedRoom, RoomDto::class.java)
                sendTargetActions(updatedRoom, session, opponentSession)
            }
        } catch (ex:Exception){
            println("Exception in handle Message : $ex")
            session.sendMessage(TextMessage(messageService.getErrorMessage(ex.message.toString())))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val roomId = session.attributes["roomId"] as String
        val sessions = roomsAndSessions[roomId]?.toMutableList() ?: mutableListOf()
        sessions.remove(session)
        roomsAndSessions[roomId] = sessions
    }

    private fun validateInputSegmentSelections(user:String, userMarkedSegment:Int, userMarkedPositions:List<Int>, room: Room){
        val boardState = room.currentBoardState!![userMarkedSegment] as Map<String,String>?
        if(boardState == null){
            throw RuntimeException("Invalid segment marked By player $user")
        }
        val previouslyMarked = boardState.keys.any { userMarkedPositions.contains(it.toInt())}
        val invalidMarkings = userMarkedPositions.any{ it >= room.segments!![userMarkedSegment] }
        if(previouslyMarked || invalidMarkings){
            throw RuntimeException("Invalid position marked By player $user")
        }
    }


    private fun updateUserMarkedPositions(player:String,playerMarkedSegment:Int,playerMarkedPositions:List<Int>,room: Room): Room {

        validateInputSegmentSelections(player,playerMarkedSegment,playerMarkedPositions,room)

        val segmentToUpdate = room.currentBoardState!![playerMarkedSegment]!! as Map<Int,String>
        val newMarkedSegment = segmentToUpdate.toMutableMap()
        newMarkedSegment.putAll(playerMarkedPositions.associateBy({it} , {player}))
        val newBoardState = room.currentBoardState!!.toMutableMap()
        newBoardState[playerMarkedSegment] = newMarkedSegment
        room.currentBoardState = newBoardState
        return room
    }

    private fun checkRoomFull(updatedRoom:RoomDto):Boolean{
        val segments = updatedRoom.segments
        var filled = true
        updatedRoom.currentBoardState!!.forEach {
            val segment = it.key
            val state = it.value as Map<Int,String>
            if(state.keys.size != segments!![segment]){
                filled = false
            }
        }
        return filled
    }

    private fun getOpponentSession(roomId:String,currentSession: WebSocketSession):WebSocketSession?{
        val sessions = roomsAndSessions[roomId] ?: listOf()
        for (webSocketSession in sessions) {
            if (webSocketSession.isOpen && currentSession.id != webSocketSession.id) {
                return webSocketSession
            }
        }
        return null
    }

    private fun sendPlayPauseActions(room: RoomDto,currentSession: WebSocketSession,opponentSession: WebSocketSession?) {
        val currentPlayer = currentSession.attributes["playerId"].toString()
        val opponentPlayer = opponentSession?.attributes?.get("playerId")?.toString()
        if(opponentSession == null){
            currentSession.sendMessage(TextMessage(messageService.getMessage(currentPlayer,Action.WON, room)))
            return
        }

        val isBoardFull = checkRoomFull(room)
        if(isBoardFull){
            currentSession.sendMessage(TextMessage(messageService.getMessage(currentPlayer,Action.LOST, room)))
            opponentSession.sendMessage(TextMessage(messageService.getMessage(opponentPlayer!!,Action.WON, room)))
        } else{
            currentSession.sendMessage(TextMessage(messageService.getMessage(currentPlayer,Action.PAUSE, room)))
            opponentSession.sendMessage(TextMessage(messageService.getMessage(opponentPlayer!!,Action.PLAY, room)))
        }
    }

    private fun sendTargetActions(room: RoomDto,currentSession: WebSocketSession,opponentSession: WebSocketSession?) {
        val currentPlayer = currentSession.attributes["playerId"].toString()
        val opponentPlayer = opponentSession?.attributes?.get("playerId")?.toString()
        if(opponentSession != null){
            currentSession.sendMessage(TextMessage(messageService.getMessage(currentPlayer,Action.TARGET, room)))
            opponentSession.sendMessage(TextMessage(messageService.getMessage(opponentPlayer!!,Action.TARGET, room)))
        }
    }

    private fun getLockedPlayerId(player: String):String{
        if(player.equals(Constants.PLAYER1,true)){
            return Constants.PLAYER1_LOCKED
        } else if(player.equals(Constants.PLAYER2,true)){
            return Constants.PLAYER2_LOCKED
        } else{
            throw RuntimeException("invalid Player $player")
        }
    }

}



