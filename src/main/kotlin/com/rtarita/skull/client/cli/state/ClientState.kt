package com.rtarita.skull.client.cli.state

import com.rtarita.skull.client.cli.action.Action
import com.rtarita.skull.common.state.GameState
import com.rtarita.skull.common.state.PlayerGameState

data class ClientState(
    val msg: String = "",
    val availableActions: Set<Action> = emptySet(),
    val serverUrl: String? = null,
    val login: String? = null,
    val token: String? = null,
    val gameid: String? = null,
    val readyForPrompt: Boolean = false,
    val subscribed: Boolean = false,
    val adminMode: Boolean = false,
    val lastPlayerGameState: PlayerGameState? = null,
    val lastGameState: GameState? = null
)
