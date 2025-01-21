/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.loggedin

sealed interface LoggedInEvents {
    data class CloseErrorDialog(val doNotShowAgain: Boolean) : LoggedInEvents
    data object CheckSlidingSyncProxyAvailability : LoggedInEvents
    data object LogoutAndMigrateToNativeSlidingSync : LoggedInEvents
}
