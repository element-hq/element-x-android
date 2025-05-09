/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaplayer.test

import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.tests.testutils.lambda.lambdaError

class FakeAudioFocusRequester(
    private val requestAudioFocusResult: (AudioFocusRequester) -> Unit = { lambdaError() },
    private val releaseAudioFocusResult: () -> Unit = { lambdaError() },
) : AudioFocus {
    override fun requestAudioFocus(mode: AudioFocusRequester) {
        requestAudioFocusResult(mode)
    }

    override fun releaseAudioFocus() {
        releaseAudioFocusResult()
    }
}
