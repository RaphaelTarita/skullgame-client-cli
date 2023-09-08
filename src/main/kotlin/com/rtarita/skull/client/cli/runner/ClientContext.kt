package com.rtarita.skull.client.cli.runner

import com.github.ajalt.mordant.terminal.Terminal
import com.rtarita.skull.client.cli.action.AdminModeAction
import com.rtarita.skull.client.cli.action.HelpAction
import com.rtarita.skull.client.cli.action.SelectServerAction
import com.rtarita.skull.client.cli.action.ShutdownAction
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.client.cli.util.secondsToMillis
import com.rtarita.skull.common.CommonConstants
import com.rtarita.skull.common.StateSignal
import com.rtarita.skull.common.condition.conflatedBooleanCondition
import com.rtarita.skull.common.condition.rendezvousSignalCondition
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.channels.Channel
import java.security.MessageDigest
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.math.min

class ClientContext {
    val http = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(CommonConstants.json)
        }
        install(WebSockets) {
            pingInterval = CommonConstants.WEBSOCKET_PING_PERIOD_SECONDS.secondsToMillis()
            maxFrameSize = CommonConstants.WEBSOCKET_MAX_FRAME_SIZE
        }
    }
    val threadpool: ExecutorService = Executors.newFixedThreadPool(min(2, Runtime.getRuntime().availableProcessors()))
    val coroutineScope = CoroutineScope(threadpool.asCoroutineDispatcher())
    val clientSignalsChannel = Channel<StateSignal.Client>(Channel.BUFFERED)
    val serverSignalsChannel = Channel<StateSignal.Server>(Channel.BUFFERED)
    val serverAcknowledgement = rendezvousSignalCondition()
    val terminal = Terminal()
    val isReadyForPrompt = conflatedBooleanCondition()
    val md: MessageDigest = MessageDigest.getInstance("SHA-256")
    val staticActions = setOf(SelectServerAction, AdminModeAction, HelpAction, ShutdownAction)
    var clientState = ClientState()
}
