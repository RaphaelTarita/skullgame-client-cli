package com.rtarita.skull.client.cli.action

import com.github.ajalt.mordant.terminal.prompt
import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState

data object RejoinGameAction : Action {
    override val name = "Rejoin Game"
    override val description = "re-establish the connection to a game which you previously joined"
    override val command = "rejoin"
    override val postAction = RefreshAction

    override suspend fun execute(context: ClientContext): ClientState {
        val gameid = context.terminal.prompt("game id") ?: return context.clientState.copy(msg = "provide a game id")

        return context.clientState.copy(
            msg = "rejoined game with id '$gameid'",
            availableActions = context.staticActions + setOf(
                NewGameAction,
                JoinGameAction,
                RejoinGameAction,
                StartGameAction,
                RefreshAction,
                MoveAction,
                if (context.clientState.subscribed) UnsubscribeAction else SubscribeAction
            ),
            gameid = gameid,
            readyForPrompt = false,
            lastPlayerGameState = null,
            lastGameState = null
        )
    }
}
