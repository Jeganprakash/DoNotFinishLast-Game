package com.donotfinishlast.webgame.controller

import com.donotfinishlast.webgame.dto.RoomDto
import com.donotfinishlast.webgame.entity.Room
import com.donotfinishlast.webgame.service.RoomService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/game")
class RoomController {

    @Autowired
    private lateinit var roomService: RoomService

    @PostMapping("/room")
    fun createRoom(httpsServletRequest: HttpServletRequest,
                   httpServletResponse: HttpServletResponse):Map<String,Any>{
        val roomDto = roomService.createRoom()
        return mapOf("roomId" to roomDto.roomId!!)
    }

}