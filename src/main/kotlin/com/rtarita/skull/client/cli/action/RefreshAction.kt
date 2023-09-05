package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.client.cli.util.authenticated
import com.rtarita.skull.common.GameState
import com.rtarita.skull.common.PlayerGameState
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.http.takeFrom

data object RefreshAction : Action {
    override val name = "Refresh"
    override val description = "refresh the current game state"
    override val command = "refresh"

    override suspend fun execute(context: ClientContext): ClientState {
        val url = context.clientState.serverUrl ?: return context.clientState.copy(msg = "provide a server url")
        val gameid = context.clientState.gameid ?: return context.clientState.copy(msg = "provide a game id")

        val response = context.http.get {
            url {
                takeFrom(url)
                path(if (context.clientState.adminMode) "masterstate" else "state", gameid)
            }
            authenticated(context)
        }

        return if (response.status == HttpStatusCode.OK) {
            val successMsg = "successfully refreshed game with id '$gameid'"
            if (context.clientState.adminMode) {
                val state = response.body<GameState>()
                context.clientState.copy(
                    msg = successMsg,
                    lastGameState = state
                )
            } else {
                val state = response.body<PlayerGameState>()
                context.clientState.copy(
                    msg = successMsg,
                    lastPlayerGameState = state,
                    lastGameState = null
                )
            }
        } else {
            context.clientState.copy(
                msg = response.bodyAsText()
            )
        }
    }
}
