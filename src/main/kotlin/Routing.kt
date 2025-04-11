package com.dkdev45

import io.ktor.server.application.*
import io.ktor.server.routing.*
import routes.createRoomRoute
import routes.getRoomsRoute
import routes.joinRoomRoute

fun Application.configureRouting() {
    routing {
        createRoomRoute()
        getRoomsRoute()
        joinRoomRoute()
    }
}
