/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.changeserver

import io.element.android.features.login.impl.accountprovider.AccountProvider

class UnauthorizedAccountProviderException(
    val accountProvider: AccountProvider,
) : Exception()
