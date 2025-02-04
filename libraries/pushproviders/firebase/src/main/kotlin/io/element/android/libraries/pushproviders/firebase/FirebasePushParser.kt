/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import io.element.android.libraries.pushproviders.api.PushData
import javax.inject.Inject

class FirebasePushParser @Inject constructor() {
    fun parse(message: Map<String, String?>): PushData? {
        val pushDataFirebase = PushDataFirebase(
            eventId = message["event_id"],
            roomId = message["room_id"],
            unread = message["unread"]?.toIntOrNull(),
            clientSecret = message["cs"],
        )
        return pushDataFirebase.toPushData()
    }
}
