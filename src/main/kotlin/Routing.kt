package com.dkdev45

import io.ktor.server.application.*
import io.ktor.server.routing.*
import routes.createRoomRoute

fun Application.configureRouting() {
    routing {
        createRoomRoute()
    }
}
