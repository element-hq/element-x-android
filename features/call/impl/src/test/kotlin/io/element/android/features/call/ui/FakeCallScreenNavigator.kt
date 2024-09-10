/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.call.ui

import io.element.android.features.call.impl.ui.CallScreenNavigator

class FakeCallScreenNavigator : CallScreenNavigator {
    var closeCalled = false
        private set

    override fun close() {
        closeCalled = true
    }
}
