package com.rtarita.skull.client.cli.action

import com.github.ajalt.mordant.terminal.YesNoPrompt
import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState

data object AdminModeAction : Action {
    override val name = "Admin Mode"
    override val description = "turn admin mode on or off"
    override val command = "admin"

    override suspend fun execute(context: ClientContext): ClientState {
        val answer = YesNoPrompt(
            prompt = "admin mode",
            terminal = context.terminal,
            default = !context.clientState.adminMode,
            promptSuffix = "? "
        ).ask() ?: false

        return context.clientState.copy(
            adminMode = answer,
            msg = "admin mode turned ${if (answer) "on" else "off"}"
        )
    }
}
