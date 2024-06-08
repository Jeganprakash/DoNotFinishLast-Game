package com.donotfinishlast.webgame.repository

import com.donotfinishlast.webgame.entity.Player
import com.donotfinishlast.webgame.entity.Room
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository:JpaRepository<Room,Int> {
    fun findByRoomId(roomId:String):Room
//    fun findByRoomIdAndPlayerSessionId(roomId:String,sessionId:Int):Player
}