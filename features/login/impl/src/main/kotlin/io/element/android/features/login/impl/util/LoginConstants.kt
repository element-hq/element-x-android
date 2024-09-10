/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.util

import io.element.android.appconfig.AuthenticationConfig
import io.element.android.features.login.impl.accountprovider.AccountProvider

val defaultAccountProvider = AccountProvider(
    url = AuthenticationConfig.DEFAULT_HOMESERVER_URL,
    subtitle = null,
    isPublic = AuthenticationConfig.DEFAULT_HOMESERVER_URL == AuthenticationConfig.MATRIX_ORG_URL,
    isMatrixOrg = AuthenticationConfig.DEFAULT_HOMESERVER_URL == AuthenticationConfig.MATRIX_ORG_URL,
)
