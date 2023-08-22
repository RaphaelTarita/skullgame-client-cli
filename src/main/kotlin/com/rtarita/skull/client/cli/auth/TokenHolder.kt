package com.rtarita.skull.client.cli.auth

import kotlinx.serialization.Serializable

@Serializable
data class TokenHolder(val token: String)