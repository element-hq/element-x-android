/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.securityandprivacy

import io.element.android.features.roomdetails.impl.securityandprivacy.SecurityAndPrivacyNavigator
import io.element.android.tests.testutils.lambda.lambdaError

class FakeSecurityAndPrivacyNavigator(
    private val openEditRoomAddressLambda: () -> Unit = { lambdaError() },
    private val closeEditorRoomAddressLambda: () -> Unit = { lambdaError() },
) : SecurityAndPrivacyNavigator {
    override fun openEditRoomAddress() {
        openEditRoomAddressLambda()
    }

    override fun closeEditorRoomAddress() {
        closeEditorRoomAddressLambda()
    }
}
