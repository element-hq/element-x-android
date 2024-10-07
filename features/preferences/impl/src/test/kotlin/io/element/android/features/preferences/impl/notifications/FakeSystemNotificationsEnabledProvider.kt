/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.notifications

class FakeSystemNotificationsEnabledProvider : SystemNotificationsEnabledProvider {
    override fun notificationsEnabled(): Boolean {
        return true
    }
}
