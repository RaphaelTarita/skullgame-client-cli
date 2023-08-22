package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState

data object ShutdownAction : Action {
    override val name = "Shutdown"
    override val description = "shuts down this client and releases all resources"
    override val command = "shutdown"

    override suspend fun execute(context: ClientContext): ClientState {
        context.http.close()

        return context.clientState.copy(
            msg = "shutdown successful",
            availableActions = emptySet()
        )
    }
}