package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.client.cli.util.authenticated
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.http.takeFrom

data object NewGameAction : Action {
    override val name = "New Game"
    override val description = "initiate a new game"
    override val command = "new"

    override suspend fun execute(context: ClientContext): ClientState {
        val url = context.clientState.serverUrl ?: return context.clientState.copy(msg = "provide a server url")

        val response = context.http.post {
            url {
                takeFrom(url)
                path("newgame")
            }
            authenticated(context)
        }

        return if (response.status == HttpStatusCode.Created) {
            val gameid = response.body<Map<String, String>>().getValue("gameid")

            context.clientState.copy(
                msg = "created a new game with id '$gameid'",
                gameid = gameid,
                lastPlayerGameState = null,
                lastGameState = null,
                availableActions = context.clientState.availableActions + StartGameAction
            )
        } else {
            context.clientState.copy(
                msg = response.bodyAsText()
            )
        }
    }
}