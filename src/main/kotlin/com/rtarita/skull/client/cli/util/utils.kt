package com.rtarita.skull.client.cli.util

import com.github.ajalt.mordant.terminal.Terminal
import com.rtarita.skull.client.cli.runner.ClientContext
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.bearerAuth
import io.ktor.http.headers

fun <E> Terminal.promptListElement(list: List<E>, prompt: String): E {
    val selectedIndex = prompt(
        prompt = prompt,
        choices = list.mapIndexed { elem, idx -> "$elem ($idx)" }
    )?.toIntOrNull() ?: return list.first()
    return list[selectedIndex]
}

fun String.truncate(maxLen: Int, replacement: String = "..."): String {
    val actualMaxLen = maxLen - replacement.length
    require(actualMaxLen >= 0) { "The truncation replacement has to be smaller than the length bound" }
    return if (length <= actualMaxLen) this else substring(0..actualMaxLen) + replacement
}

fun HttpRequestBuilder.authenticated(context: ClientContext) {
    val token = context.clientState.token ?: return
    headers {
        bearerAuth(token)
    }
}
