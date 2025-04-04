/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.networkmonitor.test

import io.element.android.features.networkmonitor.api.NetworkMonitor
import io.element.android.features.networkmonitor.api.NetworkStatus
import kotlinx.coroutines.flow.MutableStateFlow

class FakeNetworkMonitor(initialStatus: NetworkStatus = NetworkStatus.Connected) : NetworkMonitor {
    override val connectivity = MutableStateFlow(initialStatus)
}
