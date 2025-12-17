/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.oidc

import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import org.matrix.rustcomponents.sdk.AccountManagementAction as RustAccountManagementAction

fun AccountManagementAction.toRustAction(): RustAccountManagementAction {
    return when (this) {
        AccountManagementAction.Profile -> RustAccountManagementAction.Profile
        is AccountManagementAction.SessionEnd -> RustAccountManagementAction.SessionEnd(deviceId.value)
        is AccountManagementAction.SessionView -> RustAccountManagementAction.SessionView(deviceId.value)
        AccountManagementAction.SessionsList -> RustAccountManagementAction.SessionsList
    }
}
