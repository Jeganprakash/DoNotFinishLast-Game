package com.donotfinishlast.webgame.dto


class RoomDto {
    var roomId:String? = null
    var name:String? = null
    var segments:List<Int>? = null
    var activePlayers:MutableList<String> = mutableListOf()
    var currentBoardState:Map<Int,Any>? = null
}