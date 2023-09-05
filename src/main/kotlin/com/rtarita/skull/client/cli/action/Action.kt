package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState

sealed interface Action {
    val name: String
    val description: String
    val command: String

    val preActions: List<Action>
        get() = emptyList()
    val postActions: List<Action>
        get() = emptyList()

    suspend fun execute(context: ClientContext): ClientState
}
