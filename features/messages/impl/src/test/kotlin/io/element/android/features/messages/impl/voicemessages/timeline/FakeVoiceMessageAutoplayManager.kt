/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.voicemessages.timeline

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.voiceplayer.api.AutoplayTimelineItemInfo
import io.element.android.libraries.voiceplayer.api.VoiceMessageAutoplayManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class FakeVoiceMessageAutoplayManager : VoiceMessageAutoplayManager {
    private val _resetRequests = MutableSharedFlow<EventId>(extraBufferCapacity = 1)
    override val resetRequests: SharedFlow<EventId> = _resetRequests.asSharedFlow()

    override fun updateTimelineItems(items: List<AutoplayTimelineItemInfo>) = Unit

    override fun cancelAutoplay() = Unit
}
