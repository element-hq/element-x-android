/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.test.auth

import io.element.android.libraries.matrix.api.auth.HomeServerLoginCompatibilityChecker

class FakeHomeServerLoginCompatibilityChecker(
    private val checkResult: (String) -> Result<Boolean>,
) : HomeServerLoginCompatibilityChecker {
    override suspend fun check(url: String): Result<Boolean> {
        return checkResult(url)
    }
}
