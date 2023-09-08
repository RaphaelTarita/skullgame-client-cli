package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState

data object ResetGameAction : Action {
    override val name = "Reset Game"
    override val description = "resets the game state after a game ended"
    override val command = Action.NO_MANUAL_INVOCATIONS

    override suspend fun execute(context: ClientContext): ClientState {
        return context.clientState.copy(
            msg = "Game ended",
            availableActions = context.staticActions + setOf(NewGameAction, JoinGameAction, RejoinGameAction),
            gameid = null,
            readyForPrompt = true,
            lastPlayerGameState = null,
            lastGameState = null
        )
    }
}
