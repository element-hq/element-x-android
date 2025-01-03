/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.root

import io.element.android.libraries.matrix.api.oidc.AccountManagementAction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAccountManagementUrlDataSource : AccountManagementUrlDataSource {
    override fun getAccountManagementUrl(action: AccountManagementAction?): Flow<String?> {
        return flowOf(action.toString())
    }
}
