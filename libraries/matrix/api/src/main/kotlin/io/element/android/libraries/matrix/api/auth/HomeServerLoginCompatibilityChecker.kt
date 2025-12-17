/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

/**
 * Checks the homeserver's compatibility with Element X.
 */
interface HomeServerLoginCompatibilityChecker {
    /**
     * Performs the compatibility check given the homeserver's [url].
     * @return a `true` value if the homeserver is compatible, `false` if not, or a failure result if the check unexpectedly failed.
     */
    suspend fun check(url: String): Result<Boolean>
}
