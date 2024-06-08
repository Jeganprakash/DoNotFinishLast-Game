package com.donotfinishlast.webgame.configuration

import com.donotfinishlast.webgame.constants.Constants
import com.donotfinishlast.webgame.controller.MyWebSocketHandler
import com.donotfinishlast.webgame.service.RoomService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.HandshakeInterceptor

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {

    @Autowired private lateinit var roomService: RoomService

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(myHandler(), "/game/room/*")
            .setAllowedOrigins("*")
            .addInterceptors(getHandShakeInterceptor())
    }

    @Bean
    fun myHandler():WebSocketHandler{
        return MyWebSocketHandler()
    }

    @Bean
    fun getHandShakeInterceptor(): HandshakeInterceptor {
        return object : HandshakeInterceptor {
            override fun beforeHandshake(
                request: ServerHttpRequest,
                response: ServerHttpResponse,
                wsHandler: WebSocketHandler,
                attributes: MutableMap<String, Any>
            ): Boolean {
                // Get the URI segment corresponding to the auction id during handshake
                val path = request.uri.path
                val roomId = path.substring(path.lastIndexOf('/') + 1)
                val room = roomService.getRoom(roomId)
                attributes["roomId"] = roomId

                // handle concurrent modification issue
                if(room.activePlayers.isEmpty()){
                    room.activePlayers.add(Constants.PLAYER1)
                    attributes["playerId"] = Constants.PLAYER1
                } else if(room.activePlayers.size == 1){
                    room.activePlayers.add(Constants.PLAYER2)
                    attributes["playerId"] = Constants.PLAYER2
                } else{
                    attributes["playerId"] = "DummyPlayer"
                }
                roomService.saveRoom(room)

                return true
            }

            override fun afterHandshake(
                request: ServerHttpRequest,
                response: ServerHttpResponse,
                wsHandler: WebSocketHandler,
                exception: Exception?
            ) {
                // Nothing to do after handshake
            }
        }
    }
}


