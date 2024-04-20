package com.example.common.util.extensions

import androidx.compose.runtime.MutableState

fun <T> MutableState<T>.update(update: T.() -> T) {
    value = value.update()
}

