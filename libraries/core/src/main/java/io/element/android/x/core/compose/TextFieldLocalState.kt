package io.element.android.x.core.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
public fun textFieldState(stateValue: String): MutableState<String> =
    remember(stateValue) { mutableStateOf(stateValue) }
