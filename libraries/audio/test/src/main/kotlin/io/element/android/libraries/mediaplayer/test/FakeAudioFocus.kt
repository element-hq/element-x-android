/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaplayer.test

import io.element.android.libraries.audio.api.AudioFocus
import io.element.android.libraries.audio.api.AudioFocusRequester
import io.element.android.tests.testutils.lambda.lambdaError

class FakeAudioFocus(
    private val requestAudioFocusResult: (AudioFocusRequester, () -> Unit) -> Unit = { _, _ -> lambdaError() },
    private val releaseAudioFocusResult: () -> Unit = { lambdaError() },
) : AudioFocus {
    override fun requestAudioFocus(
        requester: AudioFocusRequester,
        onFocusLost: () -> Unit,
    ) {
        requestAudioFocusResult(requester, onFocusLost)
    }

    override fun releaseAudioFocus() {
        releaseAudioFocusResult()
    }
}
