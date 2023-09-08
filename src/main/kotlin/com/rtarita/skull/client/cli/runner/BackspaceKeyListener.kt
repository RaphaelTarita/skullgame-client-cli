package com.rtarita.skull.client.cli.runner

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener

class BackspaceKeyListener(private val onPress: () -> Unit) : NativeKeyListener {
    override fun nativeKeyPressed(nativeEvent: NativeKeyEvent) {
        if (nativeEvent.keyCode == NativeKeyEvent.VC_BACKSPACE) {
            onPress()
        }
    }
}
