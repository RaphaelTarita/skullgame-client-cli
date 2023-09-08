package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.common.condition.dsl.Wait
import com.rtarita.skull.common.condition.dsl.happens
import com.rtarita.skull.common.condition.dsl.until
import com.rtarita.skull.common.state.StateSignal

data object UnsubscribeAction : Action {
    override val name: String = "Unsubscribe"
    override val description: String = "unsubscribe from automatic updates of game state from the server"
    override val command: String = "unsubscribe"

    override suspend fun execute(context: ClientContext): ClientState {
        val gameid = context.clientState.gameid ?: return context.clientState.copy(msg = "provide a game id")
        context.clientSignalsChannel.send(StateSignal.Client.StopUpdates(gameid))

        Wait until context.serverAcknowledgement.happens

        return context.clientState.copy(
            msg = "successfully unsubscribed from game updates",
            availableActions = context.clientState.availableActions - UnsubscribeAction + SubscribeAction,
            readyForPrompt = true,
            subscribed = false
        )
    }
}
