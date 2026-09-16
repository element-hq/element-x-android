/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline.voicemessages.composer

import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.timeline.Timeline

fun interface VoiceMessageComposerPresenter : Presenter<VoiceMessageComposerState> {
    /**
     * Creates a presenter for one voice message composer.
     */
    interface Factory {
        /**
         * @param timelineMode the timeline the recording will be sent to, which matters for threads in particular.
         */
        fun create(timelineMode: Timeline.Mode): VoiceMessageComposerPresenter
    }
}
