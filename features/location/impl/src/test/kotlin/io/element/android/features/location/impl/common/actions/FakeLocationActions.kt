/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.common.actions

import io.element.android.features.location.api.Location

class FakeLocationActions : LocationActions {
    var sharedLocation: Location? = null
        private set

    var sharedLabel: String? = null
        private set

    var openSettingsInvocationsCount = 0
        private set

    override fun share(location: Location, label: String?) {
        sharedLocation = location
        sharedLabel = label
    }

    override fun openSettings() {
        openSettingsInvocationsCount++
    }
}
