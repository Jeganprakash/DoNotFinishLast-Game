package com.donotfinishlast.webgame.entity

import com.vladmihalcea.hibernate.type.json.JsonType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.ManyToOne
import org.hibernate.annotations.Type

@Entity
class Room{

    @Id
    var roomId:String? = null
    var name:String? = null
    var segments:List<Int>? = null
    var activePlayers:MutableList<String> = mutableListOf()

    @Type(JsonType::class)
    @Column(columnDefinition = "json")
    var currentBoardState:Map<Int,Any>? = null

}

@Entity
class Player{

    @ManyToOne
    var room:Room? = null

    var name:String? = null

    var isActive:Boolean? = null

    @Id
    var sessionId:String? = null

}
