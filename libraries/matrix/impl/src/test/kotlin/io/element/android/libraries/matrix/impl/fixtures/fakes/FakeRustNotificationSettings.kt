/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.NotificationSettings
import org.matrix.rustcomponents.sdk.NotificationSettingsDelegate

class FakeRustNotificationSettings : NotificationSettings(NoPointer) {
    private var delegate: NotificationSettingsDelegate? = null

    override fun setDelegate(delegate: NotificationSettingsDelegate?) {
        this.delegate = delegate
    }
}
