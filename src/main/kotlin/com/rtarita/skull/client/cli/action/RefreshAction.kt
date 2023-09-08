package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.client.cli.util.authenticated
import com.rtarita.skull.client.cli.util.isReadyForPrompt
import com.rtarita.skull.common.PlayerGameState
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.path
import io.ktor.http.takeFrom

data object RefreshAction : Action {
    override val name = "Refresh"
    override val description = "refresh the current game state"
    override val command = "refresh"

    private fun successMsg(gameid: String, stateResponse: HttpResponse?): String {
        val baseMsg = "successfully refreshed game with id '$gameid'"

        return if (stateResponse != null && stateResponse.status != HttpStatusCode.OK) {
            "$baseMsg (master game state unavailable)"
        } else {
            baseMsg
        }
    }

    private suspend fun ClientContext.queryState(url: String, path: String, gameid: String) = http.get {
        url {
            takeFrom(url)
            path(path, gameid)
        }
        authenticated(this@queryState)
    }

    override suspend fun execute(context: ClientContext): ClientState {
        val url = context.clientState.serverUrl ?: return context.clientState.copy(msg = "provide a server url")
        val gameid = context.clientState.gameid ?: return context.clientState.copy(msg = "provide a game id")

        val playerStateResponse = context.queryState(url, "state", gameid)
        val stateResponse = if (context.clientState.adminMode) context.queryState(url, "masterstate", gameid) else null

        return if (playerStateResponse.status == HttpStatusCode.OK) {
            val pgs = playerStateResponse.body<PlayerGameState>()

            context.clientState.copy(
                msg = successMsg(gameid, stateResponse),
                readyForPrompt = isReadyForPrompt(context.clientState, pgs),
                lastPlayerGameState = pgs,
                lastGameState = stateResponse?.body()
            )
        } else {
            context.clientState.copy(
                msg = playerStateResponse.bodyAsText(),
                readyForPrompt = true
            )
        }
    }
}
