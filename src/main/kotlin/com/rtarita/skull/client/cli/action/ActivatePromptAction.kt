package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState

data object ActivatePromptAction : Action {
    override val name: String = "Activate Prompt"
    override val description: String = "manually activate the prompt"
    override val command: String = Action.NO_MANUAL_INVOCATIONS

    override suspend fun execute(context: ClientContext): ClientState {
        return context.clientState.copy(
            readyForPrompt = true
        )
    }
}
