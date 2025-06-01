package routes

import com.dkdev45.data.Room
import com.dkdev45.logger
import com.dkdev45.server
import data.models.BasicApiResponse
import data.models.CreateRoomRequest
import data.models.RoomResponse
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import util.Constants.MAX_ROOM_SIZE

fun Route.createRoomRoute() {
    route("/api/createRoom") {
        post {
            val roomRequest = kotlin.runCatching { call.receiveNullable<CreateRoomRequest>() }.getOrNull()
            logger.info { "Request received to create room" }
            if(roomRequest == null) {
                logger.error { "Room Request is Null" }
                call.respond(HttpStatusCode.BadRequest, null)
                return@post
            }
            if(server.rooms[roomRequest.name] != null) {
                logger.error { "Room name already exist" }
                call.respond(
                    status = HttpStatusCode.OK,
                    message = BasicApiResponse(false, "Room already exists.")
                )
                return@post
            }
            if(roomRequest.maxPlayers < 2) {
                logger.error { "Room Request max players is below minimum limit of 2" }
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(false, "The minimum room size is 2.")
                )
                return@post
            }
            if(roomRequest.maxPlayers > MAX_ROOM_SIZE) {
                logger.error { "Room Request max players is above minimum limit of $MAX_ROOM_SIZE" }
                call.respond(
                    status = HttpStatusCode.OK,
                    message = BasicApiResponse(false, "The maximum room size is $MAX_ROOM_SIZE")
                )
                return@post
            }
            val room = Room(
                roomRequest.name,
                roomRequest.maxPlayers
            )
            server.rooms[roomRequest.name] = room
            logger.info { "Successfully created room with name: ${roomRequest.name} and max players: ${roomRequest.maxPlayers}" }

            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(true)
            )
        }
    }
}

fun Route.getRoomsRoute() {
    route("/api/getRooms") {
        get {
            val searchQuery = call.parameters["searchQuery"]
            logger.info { "Request received to search for room with name: $searchQuery" }
            if(searchQuery == null) {
                logger.info { "Search Query is empty" }
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val roomsResult = server.rooms.filterKeys {
                it.contains(searchQuery, ignoreCase = true)
            }
            val roomResponses = roomsResult.values.map {
                RoomResponse(
                    name = it.name,
                    maxPlayers = it.maxPlayers,
                    playerCount = it.players.size
                )
            }.sortedBy { it.name }
            logger.info { "Rooms found matching searchQuery: $roomsResult" }

            call.respond(HttpStatusCode.OK, roomResponses)
        }
    }
}

fun Route.joinRoomRoute(){
    route("/api/joinRoom") {
        get {
            val username = call.parameters["username"]
            val roomName = call.parameters["roomName"]
            logger.info { "Request to join room for $username into $roomName" }

            if(username == null || roomName == null) {
                logger.info { "Username of room name is null" }
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }

            val room = server.rooms[roomName]
            when {
                room == null -> {
                    logger.info { "Room name $roomName is invalid" }
                    call.respond(HttpStatusCode.OK, BasicApiResponse(
                        false,
                        "Room not found"
                    ))
                }
                room.containsPlayer(username) -> {
                    logger.info { "A player with name $username already exist in Room $roomName" }
                    call.respond(HttpStatusCode.OK, BasicApiResponse(
                        false,
                        "A player with this username already joined."
                    ))
                }
                room.players.size >= room.maxPlayers -> {
                    logger.info { "Room $roomName is already full" }
                    call.respond(HttpStatusCode.OK, BasicApiResponse(
                        false,
                        "This room is already full."
                    ))
                }
                else -> {
                    logger.info { "Successfully added $username to Room $roomName" }
                    call.respond(HttpStatusCode.OK, BasicApiResponse(true))
                }
            }
        }
    }
}