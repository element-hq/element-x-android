/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call.impl.data

import kotlinx.serialization.Serializable

/**
 * Payload for Element Call's `device_mute` widget action.
 *
 * - `toWidget`: request a mute configuration (`null` fields keep the current EC value).
 * - `fromWidget`: current mute state (or a reply to a request).
 */
@Serializable
data class DeviceMuteData(
    val audioEnabled: Boolean? = null,
    val videoEnabled: Boolean? = null,
)
