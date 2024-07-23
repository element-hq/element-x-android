/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.banner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class PinnedMessagesBannerPresenter @Inject constructor() : Presenter<PinnedMessagesBannerState> {

    @Composable
    override fun present(): PinnedMessagesBannerState {
        var pinnedMessageCount by remember {
            mutableIntStateOf(0)
        }
        var currentPinnedMessageIndex by rememberSaveable {
            mutableIntStateOf(0)
        }

        fun handleEvent(event: PinnedMessagesBannerEvents) {
            when (event) {
                is PinnedMessagesBannerEvents.MoveToNextPinned -> {
                    if (currentPinnedMessageIndex < pinnedMessageCount - 1) {
                        currentPinnedMessageIndex++
                    } else {
                        currentPinnedMessageIndex = 0
                    }
                }
            }
        }

        return PinnedMessagesBannerState(
            pinnedMessagesCount = pinnedMessageCount,
            currentPinnedMessageIndex = currentPinnedMessageIndex,
            eventSink = ::handleEvent
        )
    }
}
