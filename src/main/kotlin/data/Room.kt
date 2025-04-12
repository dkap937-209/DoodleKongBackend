package com.dkdev45.data

import io.ktor.websocket.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.internal.synchronized
import kotlinx.coroutines.isActive

class Room(
    val name: String,
    val maxPlayers: Int,
    var players: List<Player> = listOf()
) {

    private var phaseChangeListener:((Phase)-> Unit)? = null
    @OptIn(InternalCoroutinesApi::class)
    var phase = Phase.WAITING_FOR_PLAYERS
        set(value) {
            synchronized(field) {
                field = value
                phaseChangeListener?.let { change ->
                    change(value)
                }
            }
        }

    private fun setPhaseChangeListener(listener: (Phase) -> Unit) {
        phaseChangeListener = listener
    }

    init {
        setPhaseChangeListener { newPhase ->
            when(newPhase) {
                Phase.WAITING_FOR_PLAYERS -> waitingForPlayers()
                Phase.WAITING_FOR_START -> waitingForStart()
                Phase.NEW_ROUND -> newRound()
                Phase.GAME_RUNNING -> gameRunning()
                Phase.SHOW_WORD -> showWord()
            }
        }
    }

    suspend fun broadcast(message: String) {
        players.forEach {player ->
            if(player.socket.isActive) {
                player.socket.send(Frame.Text(message))
            }
        }
    }

    suspend fun broadcastToAllException(message: String, clientId: String) {
        players.forEach {player ->
            if(player.clientId != clientId && player.socket.isActive) {
                player.socket.send(Frame.Text(message))
            }
        }
    }

    fun containsPlayer(username: String): Boolean {
        return players.find {
            it.username == username
        } != null
    }

    private fun waitingForPlayers() {

    }

    private fun waitingForStart(){

    }

    private fun newRound(){


    }

    private fun gameRunning() {

    }

    private fun showWord() {

    }

    enum class Phase {
        WAITING_FOR_PLAYERS,
        WAITING_FOR_START,
        NEW_ROUND,
        GAME_RUNNING,
        SHOW_WORD
    }
}