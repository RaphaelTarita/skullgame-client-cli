package com.rtarita.skull.client.cli.runner

import com.github.ajalt.mordant.terminal.Terminal
import com.rtarita.skull.client.cli.action.AdminModeAction
import com.rtarita.skull.client.cli.action.HelpAction
import com.rtarita.skull.client.cli.action.SelectServerAction
import com.rtarita.skull.client.cli.action.ShutdownAction
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.common.CommonConstants
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import java.security.MessageDigest

class ClientContext {
    val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(CommonConstants.json)
        }
    }
    val terminal = Terminal()
    val md: MessageDigest = MessageDigest.getInstance("SHA-256")
    val staticActions = setOf(SelectServerAction, AdminModeAction, HelpAction, ShutdownAction)
    var clientState = ClientState()
}