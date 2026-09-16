/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.room.location

import io.element.android.libraries.matrix.api.room.location.AssetType
import org.matrix.rustcomponents.sdk.AssetType as RustAssetType

fun AssetType.into(): RustAssetType = when (this) {
    AssetType.SENDER -> RustAssetType.SENDER
    AssetType.PIN -> RustAssetType.PIN
    AssetType.UNKNOWN -> RustAssetType.UNKNOWN
}

fun RustAssetType.into(): AssetType = when (this) {
    RustAssetType.SENDER -> AssetType.SENDER
    RustAssetType.PIN -> AssetType.PIN
    RustAssetType.UNKNOWN -> AssetType.UNKNOWN
}
