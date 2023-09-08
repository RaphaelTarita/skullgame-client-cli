package com.rtarita.skull.client.cli.util

import com.github.ajalt.mordant.terminal.Terminal
import com.rtarita.skull.client.cli.action.StartGameAction
import com.rtarita.skull.client.cli.runner.ClientContext
import com.rtarita.skull.client.cli.state.ClientState
import com.rtarita.skull.common.state.PlayerGameState
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.http.headers
import kotlinx.coroutines.sync.Mutex
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun Long.secondsToMillis() = Duration.convert(toDouble(), DurationUnit.SECONDS, DurationUnit.MILLISECONDS).toLong()

fun String.truncate(maxLen: Int, replacement: String = "..."): String {
    val actualMaxLen = maxLen - replacement.length
    require(actualMaxLen >= 0) { "The truncation replacement has to be smaller than the length bound" }
    return if (length <= actualMaxLen) this else substring(0..actualMaxLen) + replacement
}

@OptIn(ExperimentalContracts::class)
inline fun <T> Mutex.tryWithLock(owner: Any? = null, action: () -> T): T? {
    contract {
        callsInPlace(action, InvocationKind.AT_MOST_ONCE)
    }

    val acquired = tryLock(owner)
    return if (acquired) {
        try {
            action()
        } finally {
            unlock(owner)
        }
    } else {
        null
    }
}

fun HttpRequestBuilder.authenticated(context: ClientContext) {
    val token = context.clientState.token ?: return
    headers {
        bearerAuth(token)
    }
}

fun <E : Any> Terminal.promptListElement(list: List<E>, prompt: String, transform: (E) -> String = Any::toString): E {
    val selected = prompt(
        prompt = prompt,
        choices = list.map { transform(it) }
    ) ?: return list.first()
    return list.first { transform(it) == selected }
}

fun isReadyForPrompt(clientState: ClientState, playerGameState: PlayerGameState? = clientState.lastPlayerGameState): Boolean {
    if (StartGameAction in clientState.availableActions) return true
    if (!clientState.subscribed) return true
    val pgs = playerGameState ?: return false
    val hasPlayedFirstCard = pgs.ownCardsOnTable.isNotEmpty()
    return (pgs.currentTurn == -1 && !hasPlayedFirstCard) || pgs.currentTurn == pgs.playerIndex
}
