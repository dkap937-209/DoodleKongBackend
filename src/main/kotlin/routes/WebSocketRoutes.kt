package routes

import com.dkdev45.data.Player
import com.dkdev45.data.Room
import com.dkdev45.gson
import com.dkdev45.logger
import com.dkdev45.server
import com.dkdev45.session.DrawingSession
import com.google.gson.JsonParser
import data.models.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import util.Constants.TYPE_ANNOUNCEMENT
import util.Constants.TYPE_CHAT_MESSAGE
import util.Constants.TYPE_CHOSEN_WORD
import util.Constants.TYPE_DISCONNECT_REQUEST
import util.Constants.TYPE_DRAW_ACTION
import util.Constants.TYPE_DRAW_DATA
import util.Constants.TYPE_GAME_STATE
import util.Constants.TYPE_JOIN_ROOM_HANDSHAKE
import util.Constants.TYPE_PHASE_CHANGE
import util.Constants.TYPE_PING


fun Route.gameWebSocketRoute() {
    route("/ws/draw") {
        standardWebSocket { socket, clientId, message, payload ->
            when(payload) {
                is JoinRoomHandshake -> {
                    logger.info { "JoinRoomHandshake received. RoomName: ${payload.roomName}, username: ${payload.username}" }
                    val room = server.rooms[payload.roomName]
                    if(room == null) {
                        logger.error { "JoinRoomHandshake: No room found for ${payload.roomName}" }
                        val gameError = GameError(
                            GameError.ERROR_ROOM_NOT_FOUND
                        )
                        socket.send(Frame.Text(gson.toJson(gameError)))
                        return@standardWebSocket
                    }

                    val player = Player(
                        payload.username,
                        socket,
                        payload.clientId
                    )
                    server.playerJoined(player)
                    if(!room.containsPlayer(player.username)) {
                        room.addPlayer(player.clientId, player.username, socket)
                        logger.info { "Added player ${player.username} to $room" }
                    } else {
                        logger.info { "A player with name ${player.username} already exist. Pinging them." }
                        val playerInRoom = room.players.find { it.clientId == clientId }
                        playerInRoom?.socket = socket
                        playerInRoom?.startPinging()
                    }
                }
                is DrawData -> {
                    val room = server.rooms[payload.roomName] ?: return@standardWebSocket
                    if(room.phase == Room.Phase.GAME_RUNNING) {
                        room.broadcastToAllExcept(message, clientId)
                        room.addSerializedDrawInfo(message)
                    }
                    room.lastDrawData = payload
                }
                is DrawAction -> {
                    val room = server.getRoomWithClientId(clientId) ?: return@standardWebSocket
                    room.broadcastToAllExcept(message, clientId)
                    room.addSerializedDrawInfo(message)
                }
                is ChosenWord -> {
                    val room = server.rooms[payload.roomName] ?: return@standardWebSocket
                    room.setWordAndSwitchToGameRunning(payload.chosenWord)
                }
                is ChatMessage -> {
                    val room = server.rooms[payload.roomName] ?: return@standardWebSocket
                    if(!room.checkWordAndNotifyPlayers(payload)) {
                        room.broadcast(message)
                    }
                }
                is Ping -> {
                    server.players[clientId]?.receivePong()
                }
                is DisconnectRequest -> {
                    logger.info { "Player with clientId $clientId has left the room" }
                    server.playerLeft(clientId, true)
                }
            }
        }
    }
}
fun Route.standardWebSocket(
    handleFrame: suspend (
        socket: DefaultWebSocketServerSession,
        clientId: String,
        message: String,
        payload: BaseModel
    ) -> Unit
) {
    webSocket {
        val session = call.sessions.get<DrawingSession>()
        if(session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No sessions."))
            return@webSocket
        }

        try {
            incoming.consumeEach { frame ->
                if(frame is Frame.Text) {
                    val message = frame.readText()
                    val jsonObject = JsonParser.parseString(message).asJsonObject
                    val type = when(jsonObject.get("type").asString) {
                        TYPE_CHAT_MESSAGE -> ChatMessage::class.java
                        TYPE_DRAW_DATA -> DrawData::class.java
                        TYPE_ANNOUNCEMENT -> Announcement::class.java
                        TYPE_JOIN_ROOM_HANDSHAKE -> JoinRoomHandshake::class.java
                        TYPE_PHASE_CHANGE -> PhaseChange::class.java
                        TYPE_CHOSEN_WORD -> ChosenWord::class.java
                        TYPE_GAME_STATE -> GameState::class.java
                        TYPE_PING -> Ping::class.java
                        TYPE_DISCONNECT_REQUEST -> DisconnectRequest::class.java
                        TYPE_DRAW_ACTION -> DrawAction::class.java
                        else -> BaseModel::class.java
                    }
                    val payload = gson.fromJson(message, type)
                    handleFrame(this, session.clientId, message, payload)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        finally {
            // Handle disconnect
            val playerWithClientId = server.getRoomWithClientId(session.clientId)?.players?.find {
                it.clientId == session.clientId
            }

            if(playerWithClientId != null) {
                server.playerLeft(session.clientId)
            }
        }
    }
}