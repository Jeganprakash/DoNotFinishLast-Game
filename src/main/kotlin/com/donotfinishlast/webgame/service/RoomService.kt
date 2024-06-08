package com.donotfinishlast.webgame.service

import com.donotfinishlast.webgame.dto.RoomDto
import com.donotfinishlast.webgame.entity.Room
import com.donotfinishlast.webgame.repository.RoomRepository
import org.modelmapper.ModelMapper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import kotlin.random.Random

@Service
class RoomService {
    @Autowired
    private lateinit var roomRepository: RoomRepository

    @Autowired
    private lateinit var modelMapper: ModelMapper


    @Transactional
    fun createRoom():RoomDto{
        val room = Room()
        room.roomId = generateRandomRoomName(5)
        val size = Random.nextInt(3,5)  // Change this to adjust the size of the list
        room.segments = List(size) { Random.nextInt(3,9) }
        room.currentBoardState = populateBoardState(room.segments!!)
        roomRepository.save(room)
        return modelMapper.map(room,RoomDto::class.java)
    }

    fun getRoom(roomId:String):Room{
        val room = roomRepository.findByRoomId(roomId)
        if(room == null){
            throw RuntimeException("Invalid Room Id")
        }
        return room
    }

    fun getRoomDto(roomId:String):RoomDto{
        val room = roomRepository.findByRoomId(roomId)
        if(room == null){
            throw RuntimeException("Invalid Room Id")
        }
        return modelMapper.map(room,RoomDto::class.java)
    }


    fun saveRoom(room: Room):RoomDto{
        val room = roomRepository.saveAndFlush(room)
        return modelMapper.map(room,RoomDto::class.java)
    }

    fun generateRandomRoomName(length: Int): String {
        val allowedChars = ('A'..'Z') + ('0'..'9') // You can customize the character set as needed
        return (1..length)
            .map { allowedChars.random() }
            .joinToString("")
    }

    private fun populateBoardState(segments:List<Int>):Map<Int,Map<Int,String>>{
        return segments.indices.associateBy({it},{ mapOf() })
    }
}