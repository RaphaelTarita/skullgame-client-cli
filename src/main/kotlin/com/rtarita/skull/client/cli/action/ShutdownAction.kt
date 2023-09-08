package com.rtarita.skull.client.cli.action

import com.github.kwhat.jnativehook.GlobalScreen
import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import kotlinx.coroutines.cancel

data object ShutdownAction : Action {
    override val name = "Shutdown"
    override val description = "shuts down this client and releases all resources"
    override val command = "shutdown"

    override suspend fun execute(context: ClientContext): ClientState {
        context.http.close()
        context.clientSignalsChannel.close()
        context.serverSignalsChannel.close()
        context.serverAcknowledgement.close()
        GlobalScreen.unregisterNativeHook()
        context.isReadyForPrompt.given()
        context.isReadyForPrompt.close()
        context.coroutineScope.cancel()
        context.threadpool.shutdown()

        return context.clientState.copy(
            msg = "shutdown successful",
            availableActions = emptySet()
        )
    }
}
