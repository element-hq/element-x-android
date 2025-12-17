/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.factories

import io.element.android.libraries.matrix.test.A_DEVICE_ID
import io.element.android.libraries.matrix.test.A_HOMESERVER_URL
import io.element.android.libraries.matrix.test.A_USER_ID
import org.matrix.rustcomponents.sdk.Session
import org.matrix.rustcomponents.sdk.SlidingSyncVersion

internal fun aRustSession(
    proxy: SlidingSyncVersion = SlidingSyncVersion.NONE,
    accessToken: String = "accessToken",
    refreshToken: String = "refreshToken",
): Session {
    return Session(
        accessToken = accessToken,
        refreshToken = refreshToken,
        userId = A_USER_ID.value,
        deviceId = A_DEVICE_ID.value,
        homeserverUrl = A_HOMESERVER_URL,
        oidcData = null,
        slidingSyncVersion = proxy,
    )
}
