package com.donotfinishlast.webgame.service

import com.donotfinishlast.webgame.dto.RoomDto
import com.google.gson.Gson
import org.springframework.stereotype.Service

@Service
class MessageService {

    private val gson = Gson()

    fun getMessage(playerId:String,action: Action, room: RoomDto? = null):String{
        val messageMap = mapOf(
            "action" to action.name,
            "segments" to room?.segments,
            "board" to room?.currentBoardState,
            "player" to playerId
        )
        // Convert the map to a JSON string
        return gson.toJson(messageMap)
    }

    fun getErrorMessage(message:String):String{
        val messageMap = mapOf(
           "action" to Action.ERROR.name,
            "message" to message
        )
        // Convert the map to a JSON string
        return gson.toJson(messageMap)
    }
}

enum class Action {
    WAIT, PLAY, PAUSE, SUBMIT, WON, LOST, FULL, SELECT, ERROR, TARGET
}