package io.element.android.x.presentation

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.Flow

interface Presenter<State, Event> {
    @Composable
    fun present(events: Flow<Event>): State
}
