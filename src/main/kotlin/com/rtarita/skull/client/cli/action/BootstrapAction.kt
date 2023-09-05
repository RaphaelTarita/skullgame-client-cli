package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState


data object BootstrapAction : Action {
    override val name = "Bootstrap"
    override val description = "Is run at boot time of the application and supplies the first set of possible actions"
    override val command = "bootstrap"

    override suspend fun execute(context: ClientContext): ClientState {
        return context.clientState.copy(
            msg = "Application ready",
            availableActions = context.staticActions
        )
    }
}
