package com.dkdev45

import DrawingServer
import com.google.gson.Gson
import io.ktor.server.application.*
import mu.KotlinLogging

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val server = DrawingServer()
val gson = Gson()
val logger = KotlinLogging.logger {}

fun Application.module() {
    configureSession()
    configureSerialization()
    configureMonitoring()
    configureSockets()
    configureRouting()
}
