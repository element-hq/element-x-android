/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications.sound

import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier.CopyResult
import io.element.android.libraries.push.api.notifications.sound.NotificationSoundCopier.SoundSlot
import io.element.android.tests.testutils.lambda.lambdaError

class FakeNotificationSoundCopier(
    private val copyLambda: (String, SoundSlot) -> CopyResult = { _, _ -> lambdaError() },
    private val deleteStoredSoundForLambda: (SoundSlot) -> Unit = {},
) : NotificationSoundCopier {
    override suspend fun copyToAppFiles(sourceUriString: String, slot: SoundSlot): CopyResult = copyLambda(sourceUriString, slot)

    override suspend fun deleteStoredSoundFor(slot: SoundSlot) {
        deleteStoredSoundForLambda(slot)
    }
}
