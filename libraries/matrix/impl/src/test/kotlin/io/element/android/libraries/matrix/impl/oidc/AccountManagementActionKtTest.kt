/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.oidc

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import io.element.android.libraries.matrix.test.A_DEVICE_ID
import org.junit.Test
import org.matrix.rustcomponents.sdk.AccountManagementAction as RustAccountManagementAction

class AccountManagementActionKtTest {
    @Test
    fun `test AccountManagementAction to RustAccountManagementAction`() {
        assertThat(AccountManagementAction.Profile.toRustAction())
            .isEqualTo(RustAccountManagementAction.Profile)
        assertThat(AccountManagementAction.SessionEnd(A_DEVICE_ID).toRustAction())
            .isEqualTo(RustAccountManagementAction.SessionEnd(A_DEVICE_ID.value))
        assertThat(AccountManagementAction.SessionView(A_DEVICE_ID).toRustAction())
            .isEqualTo(RustAccountManagementAction.SessionView(A_DEVICE_ID.value))
        assertThat(AccountManagementAction.SessionsList.toRustAction())
            .isEqualTo(RustAccountManagementAction.SessionsList)
    }
}
