/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.logout.api.direct

sealed interface DirectLogoutEvents {
    data class Logout(val ignoreSdkError: Boolean) : DirectLogoutEvents
    data object CloseDialogs : DirectLogoutEvents
}
