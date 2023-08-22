package com.rtarita.skull.client.cli.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginCredentials(
    val id: String,
    val passHash: String
)