package com.dkdev45

import com.dkdev45.session.DrawingSession
import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

fun Application. configureSession() {
    install(Sessions) {
        cookie<DrawingSession>("SESSION")
    }
    intercept(Plugins) {
        if(call.sessions.get<DrawingSession>() == null) {
            val clientId = call.parameters["client_id"] ?: ""
            call.sessions.set(DrawingSession(clientId, generateNonce()))
        }
    }
}
