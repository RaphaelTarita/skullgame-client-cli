package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.client.cli.util.isReadyForPrompt
import com.rtarita.skull.common.condition.dsl.Wait
import com.rtarita.skull.common.condition.dsl.happens
import com.rtarita.skull.common.condition.dsl.until
import com.rtarita.skull.common.state.StateSignal

data object SubscribeAction : Action {
    override val name = "Subscribe"
    override val description = "subscribe to automatic updates of game state from the server"
    override val command = "subscribe"

    override suspend fun execute(context: ClientContext): ClientState {
        val gameid = context.clientState.gameid ?: return context.clientState.copy(msg = "provide a game id")
        context.clientSignalsChannel.send(StateSignal.Client.RequestUpdates(gameid))

        Wait until context.serverAcknowledgement.happens

        val result = context.clientState.copy(
            msg = "successfully subscribed to game updates",
            availableActions = context.clientState.availableActions - SubscribeAction + UnsubscribeAction,
            subscribed = true,
        )

        return result.copy(
            readyForPrompt = result.readyForPrompt && isReadyForPrompt(result)
        )
    }
}
