/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.networkmonitor.api

import kotlinx.coroutines.flow.StateFlow

/**
 * Monitors the network status of the device, providing the current network connectivity status as a flow.
 *
 * **Note:** network connectivity does not imply internet connectivity. The device can be connected to a network that can't reach the homeserver.
 */
interface NetworkMonitor {
    /**
     * A flow containing the current network connectivity status.
     */
    val connectivity: StateFlow<NetworkStatus>

    /**
     * Checks if the active network is being blocked by Doze, even if it's available.
     */
    val isNetworkBlocked: StateFlow<Boolean>

    /**
     * A flow indicating whether the app is running in an air-gapped environment.
     * An air-gapped environment is an environment that is not connected to the internet, and where the app can only communicate with a limited set of servers.
     */
    val isInAirGappedEnvironment: StateFlow<Boolean>
}
