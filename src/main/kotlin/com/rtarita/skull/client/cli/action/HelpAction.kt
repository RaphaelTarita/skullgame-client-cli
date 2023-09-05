package com.rtarita.skull.client.cli.action

import com.github.ajalt.mordant.table.table
import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState

data object HelpAction : Action {
    override val name = "Help"
    override val description = "display descriptions for currently available actions"
    override val command = "help"

    override suspend fun execute(context: ClientContext): ClientState {
        val table = table {
            header {
                row("Name", "Command", "Description")
            }

            body {
                for (action in context.clientState.availableActions) {
                    row(action.name, action.command, action.description)
                }
            }
        }

        context.terminal.println(table)

        return context.clientState.copy(
            msg = "printed help"
        )
    }
}
