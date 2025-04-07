package routes

import com.dkdev45.data.Room
import com.dkdev45.server
import data.models.BasicApiResponse
import data.models.CreateRoomRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import util.Constants.MAX_ROOM_SIZE

fun Route.createRoomRoute() {
    route("/api/createRoom") {
        post {
            val roomRequest = kotlin.runCatching { call.receiveNullable<CreateRoomRequest>() }.getOrNull()
            if(roomRequest == null) {
                call.respond(HttpStatusCode.BadRequest, null)
                return@post
            }
            if(server.rooms[roomRequest.name] != null) {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = BasicApiResponse(false, "Room already exists.")
                )
                return@post
            }
            if(roomRequest.maxPlayers < 2) {
                call.respond(
                    HttpStatusCode.OK,
                    BasicApiResponse(false, "The minimum room size is 2.")
                )
                return@post
            }
            if(roomRequest.maxPlayers > MAX_ROOM_SIZE) {
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
            println("Room created: ${roomRequest.name}")

            call.respond(
                HttpStatusCode.OK,
                BasicApiResponse(true)
            )
        }
    }
}