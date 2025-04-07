package com.dkdev45

import DrawingServer
import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

val server = DrawingServer()

fun Application.module() {
    configureSession()
    configureSerialization()
    configureMonitoring()
    configureSockets()
    configureRouting()
}
