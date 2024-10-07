/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

sealed interface TroubleshootNotificationsEvents {
    data object StartTests : TroubleshootNotificationsEvents
    data object RetryFailedTests : TroubleshootNotificationsEvents
    data class QuickFix(val testIndex: Int) : TroubleshootNotificationsEvents
}
