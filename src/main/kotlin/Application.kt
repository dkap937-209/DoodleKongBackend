package com.dkdev45

import io.ktor.server.application.*

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    configureSession()
    configureSerialization()
    configureMonitoring()
    configureSockets()
    configureRouting()
}
