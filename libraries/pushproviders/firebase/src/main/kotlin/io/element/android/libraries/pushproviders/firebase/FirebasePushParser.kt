/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

import dev.zacsweers.metro.Inject
import io.element.android.libraries.pushproviders.api.PushData

@Inject
class FirebasePushParser {
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
