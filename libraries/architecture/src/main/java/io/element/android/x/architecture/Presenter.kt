package io.element.android.x.architecture

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

interface Presenter<State> {
    @Composable
    fun present(): State
}
