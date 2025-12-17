/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

object LockScreenConfig {
    /** Whether the PIN is mandatory or not. */
    const val IS_PIN_MANDATORY: Boolean = false

    /** Set of forbidden PIN codes. */
    val FORBIDDEN_PIN_CODES: Set<String> = setOf("0000", "1234")

    /** The size of the PIN. */
    const val PIN_SIZE: Int = 4

    /** Number of attempts before the user is logged out. */
    const val MAX_PIN_CODE_ATTEMPTS_BEFORE_LOGOUT: Int = 3

    /** Time period before locking the app once backgrounded. */
    val GRACE_PERIOD: Duration = 2.minutes

    /** Authentication with strong methods (fingerprint, some face/iris unlock implementations) is supported. */
    const val IS_STRONG_BIOMETRICS_ENABLED: Boolean = true

    /** Authentication with weak methods (most face/iris unlock implementations) is supported. */
    const val IS_WEAK_BIOMETRICS_ENABLED: Boolean = true
}
