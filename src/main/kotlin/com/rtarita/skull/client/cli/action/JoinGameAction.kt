package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.client.cli.util.authenticated
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.http.takeFrom

data object JoinGameAction : Action {
    override val name = "Join Game"
    override val description = "join an existing game"
    override val command = "join"

    override suspend fun execute(context: ClientContext): ClientState {
        val url = context.clientState.serverUrl ?: return context.clientState.copy(msg = "provide a server url")
        val gameid = context.terminal.prompt("game id") ?: return context.clientState.copy(msg = "provide a game id")

        val response = context.http.post {
            url {
                takeFrom(url)
                path("join", gameid)
            }
            authenticated(context)
        }

        return if (response.status == HttpStatusCode.OK) {
            context.clientState.copy(
                msg = response.bodyAsText(),
                gameid = gameid,
                availableActions = context.clientState.availableActions + setOf(RefreshAction, MoveAction)
            )
        } else {
            context.clientState.copy(
                msg = response.bodyAsText()
            )
        }
    }
}