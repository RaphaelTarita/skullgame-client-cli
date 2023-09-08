package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState

data object SelectServerAction : Action {
    override val name = "Select Server"
    override val description = "select a game server by supplying its URL"
    override val command = "server"

    override suspend fun execute(context: ClientContext): ClientState {
        val url = context.terminal.prompt("server url") ?: return context.clientState.copy(msg = "provide a server url")

        return context.clientState.copy(
            msg = "successfully switched to server '$url'",
            availableActions = context.staticActions + LoginAction,
            login = null,
            token = null,
            gameid = null,
            lastPlayerGameState = null,
            lastGameState = null,
            serverUrl = url
        )
    }
}
