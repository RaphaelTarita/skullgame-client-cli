package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState

sealed interface Action {
    companion object {
        const val NO_MANUAL_INVOCATIONS = "__AUTOMATIC__"
    }

    val name: String
    val description: String
    val command: String
    val postAction: Action?
        get() = null

    suspend fun execute(context: ClientContext): ClientState
}
