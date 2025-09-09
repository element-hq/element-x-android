/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush

import dev.zacsweers.metro.Inject
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.pushproviders.api.PushData
import kotlinx.serialization.json.Json

@Inject class UnifiedPushParser {
    private val json by lazy { Json { ignoreUnknownKeys = true } }

    fun parse(message: ByteArray, clientSecret: String): PushData? {
        return tryOrNull { json.decodeFromString<PushDataUnifiedPush>(String(message)) }?.toPushData(clientSecret)
    }
}
