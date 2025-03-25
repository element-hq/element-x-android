/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

object ElementCallConfig {
    /**
     * The default base URL for the Element Call service.
     */
    const val DEFAULT_BASE_URL = "https://call.element.io/room"

    /**
     * The default duration of a ringing call in seconds before it's automatically dismissed.
     */
    const val RINGING_CALL_DURATION_SECONDS = 90
}
