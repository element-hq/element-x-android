/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.messagetextsize

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import io.element.android.libraries.preferences.api.store.MessageTextSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class MessageTextSizePresenter(
    private val appPreferencesStore: AppPreferencesStore,
    @SessionCoroutineScope
    private val sessionCoroutineScope: CoroutineScope,
) : Presenter<MessageTextSizeState> {
    @Composable
    override fun present(): MessageTextSizeState {
        val storedSizeSp by remember {
            appPreferencesStore.getMessageTextSizeFlow()
        }.collectAsState(initial = MessageTextSize.DEFAULT)

        val pickedSizeSp = remember { mutableStateOf<Int?>(null) }
        val textSizeSp = pickedSizeSp.value ?: storedSizeSp

        fun handleEvent(event: MessageTextSizeEvent) {
            when (event) {
                is MessageTextSizeEvent.SetTextSize -> pickedSizeSp.value = event.sizeSp
                // Store it as soon as the slider is let go rather than when the screen goes away: the screen is torn down only once Appyx has
                // finished animating it out, which leaves the setting showing its old value for a moment afterwards.
                MessageTextSizeEvent.CommitTextSize -> {
                    val picked = pickedSizeSp.value
                    if (picked != null && picked != storedSizeSp) {
                        sessionCoroutineScope.launch {
                            appPreferencesStore.setMessageTextSize(picked)
                        }
                    }
                }
            }
        }

        return MessageTextSizeState(
            textSizeSp = textSizeSp,
            eventSink = ::handleEvent,
        )
    }
}
