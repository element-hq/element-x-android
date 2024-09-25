/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
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
