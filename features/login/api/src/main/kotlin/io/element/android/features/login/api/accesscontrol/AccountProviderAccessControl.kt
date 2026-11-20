/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.api.accesscontrol

import io.element.android.libraries.matrix.api.accountprovider.AccountProvider

/**
 * Enforces the restriction an enterprise deployment can put on which account providers the user may sign in to.
 */
interface AccountProviderAccessControl {
    /**
     * Whether sign-in to this provider is permitted; `true` on a build with no such restriction.
     *
     * @param accountProvider the account provider the user is trying to use.
     */
    suspend fun isAllowedToConnectToAccountProvider(accountProvider: AccountProvider): Boolean
}
