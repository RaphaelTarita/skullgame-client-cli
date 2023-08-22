package com.rtarita.skull.client.cli.action

import com.rtarita.skull.client.cli.auth.LoginCredentials
import com.rtarita.skull.client.cli.auth.TokenHolder
import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.http.takeFrom

data object LoginAction : Action {
    override val name = "Login"
    override val description = "logs an user in, using username and password"
    override val command = "login"

    @OptIn(ExperimentalStdlibApi::class)
    override suspend fun execute(context: ClientContext): ClientState {
        val url = context.clientState.serverUrl ?: return context.clientState.copy(msg = "provide a server url")
        val userid = context.terminal.prompt("user id") ?: return context.clientState.copy(msg = "provide a user id")
        val password = context.terminal.prompt("password", hideInput = true) ?: return context.clientState.copy(msg = "provide a password")

        val passHash = context.md
            .digest(password.toByteArray(Charsets.UTF_8))
            .toHexString()

        val result = context.http.post {
            url {
                takeFrom(url)
                path("login")
            }
            contentType(ContentType.Application.Json)
            setBody(LoginCredentials(userid, passHash))
        }

        return if (result.status == HttpStatusCode.OK) {
            val body = result.body<TokenHolder>()
            context.clientState.copy(
                msg = "successfully logged in as '$userid'",
                login = userid,
                token = body.token,
                availableActions = context.staticActions + setOf(NewGameAction, JoinGameAction, RejoinGameAction)
            )
        } else {
            context.clientState.copy(msg = result.bodyAsText())
        }
    }
}