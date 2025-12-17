/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.location

import io.element.android.libraries.matrix.api.room.location.AssetType

fun AssetType.toInner(): org.matrix.rustcomponents.sdk.AssetType = when (this) {
    AssetType.SENDER -> org.matrix.rustcomponents.sdk.AssetType.SENDER
    AssetType.PIN -> org.matrix.rustcomponents.sdk.AssetType.PIN
}
