/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.networkmonitor.api

/**
 * Network connectivity status of the device.
 *
 * **Note:** this is *network* connectivity status, not *internet* connectivity status.
 */
enum class NetworkStatus {
    /**
     * The device is connected to a network.
     */
    Connected,

    /**
     * The device is not connected to any networks.
     */
    Disconnected
}
