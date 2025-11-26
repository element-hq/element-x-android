/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.user.editprofile

import io.element.android.tests.testutils.lambda.lambdaError

class FakeEditUserProfileNavigator(
    val closeLambda: () -> Unit = { lambdaError() }
) : EditUserProfileNavigator {
    override fun close() = closeLambda()
}
