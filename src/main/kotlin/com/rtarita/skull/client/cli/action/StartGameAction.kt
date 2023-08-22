package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.client.cli.util.authenticated
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.http.takeFrom

data object StartGameAction : Action {
    override val name = "Start Game"
    override val description = "start a game that you created"
    override val command = "start"

    override val postActions = listOf(RefreshAction)

    override suspend fun execute(context: ClientContext): ClientState {
        val url = context.clientState.serverUrl ?: return context.clientState.copy(msg = "provide a server url")
        val gameid = context.clientState.gameid ?: return context.clientState.copy(msg = "provide a game id")

        val response = context.http.post {
            url {
                takeFrom(url)
                path("startgame", gameid)
            }
            authenticated(context)
        }

        return if (response.status == HttpStatusCode.OK) {
            context.clientState.copy(
                msg = response.bodyAsText(),
                availableActions = context.clientState.availableActions - StartGameAction + setOf(RefreshAction, MoveAction)
            )
        } else {
            context.clientState.copy(
                msg = response.bodyAsText()
            )
        }
    }
}