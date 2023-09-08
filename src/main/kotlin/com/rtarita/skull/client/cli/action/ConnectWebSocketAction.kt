package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.client.cli.util.authenticated
import com.rtarita.skull.common.StateSignal
import com.rtarita.skull.common.condition.dsl.Wait
import com.rtarita.skull.common.condition.dsl.happens
import com.rtarita.skull.common.condition.dsl.until
import com.rtarita.skull.common.condition.rendezvousSignalCondition
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.URLProtocol
import io.ktor.http.path
import io.ktor.http.takeFrom
import io.ktor.websocket.Frame
import io.ktor.websocket.readBytes
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch

data object ConnectWebSocketAction : Action {
    override val name: String = "Connect WebSocket"
    override val description: String = "open a WebSocket connection to the server"
    override val command: String = Action.NO_MANUAL_INVOCATIONS

    override suspend fun execute(context: ClientContext): ClientState {
        val url = context.clientState.serverUrl ?: return context.clientState.copy(msg = "provide a server url")

        val connectionSuccessful = rendezvousSignalCondition()
        context.coroutineScope.launch {
            context.http.webSocket({
                url {
                    takeFrom(url)
                    protocol = if (protocol == URLProtocol.HTTPS) URLProtocol.WSS else URLProtocol.WS
                    path("subscribe")
                }
                authenticated(context)
            }) {
                connectionSuccessful.signalAndClose()
                val serverListenJob = serverListen(context.serverSignalsChannel)
                val clientListenJob = clientListen(context.clientSignalsChannel)

                serverListenJob.join()
                clientListenJob.join()
            }
        }

        Wait until connectionSuccessful.happens
        return context.clientState.copy(msg = "websocket connection established")
    }

    private fun DefaultClientWebSocketSession.serverListen(channel: SendChannel<StateSignal.Server>) = launch {
        for (frame in incoming) {
            if (frame is Frame.Binary) {
                channel.send(StateSignal.deserializeServer(frame.readBytes()))
            }
        }
    }

    private fun DefaultClientWebSocketSession.clientListen(channel: ReceiveChannel<StateSignal.Client>) = launch {
        for (signal in channel) {
            send(Frame.Binary(true, signal.serialize()))
        }
    }
}
