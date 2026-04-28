/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.test.notifications

import io.element.android.libraries.preferences.api.store.NotificationSound
import io.element.android.libraries.push.api.notifications.NotificationSoundUpdater
import io.element.android.tests.testutils.lambda.lambdaError

class FakeNotificationSoundUpdater(
    private val recreateNoisyChannelLambda: (sound: NotificationSound, version: Int) -> Unit = { _, _ -> lambdaError() },
    private val recreateRingingCallChannelLambda: (sound: NotificationSound, version: Int) -> Unit = { _, _ -> lambdaError() },
) : NotificationSoundUpdater {
    override fun recreateNoisyChannel(sound: NotificationSound, version: Int) {
        recreateNoisyChannelLambda(sound, version)
    }

    override fun recreateRingingCallChannel(sound: NotificationSound, version: Int) {
        recreateRingingCallChannelLambda(sound, version)
    }
}
