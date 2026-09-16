/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.api.test

/**
 * Lets a troubleshooting test's quick fix send the user to another screen, without the test itself knowing about navigation.
 */
interface NotificationTroubleshootNavigator {
    /** Opens the blocked users screen, so the user can unblock someone whose messages were being filtered out. */
    fun navigateToBlockedUsers()
}
