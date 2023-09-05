package com.rtarita.skull.client.cli

import com.rtarita.skull.client.cli.runner.ClientRunner
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    ClientRunner.run()
}
