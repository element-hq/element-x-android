/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.licenses.impl.list

import io.element.android.features.licenses.impl.LicensesProvider
import io.element.android.features.licenses.impl.model.DependencyLicenseItem
import io.element.android.tests.testutils.lambda.lambdaError

class FakeLicensesProvider(
    private val provideResult: () -> List<DependencyLicenseItem> = { lambdaError() }
) : LicensesProvider {
    override suspend fun provides(): List<DependencyLicenseItem> {
        return provideResult()
    }
}
