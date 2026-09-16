/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.analytics

import io.element.android.libraries.core.data.ByteSize

/**
 * The sizes of the different stores (DBs) in the SDK.
 */
data class SdkStoreSizes(
    val stateStore: ByteSize?,
    val eventCacheStore: ByteSize?,
    val mediaStore: ByteSize?,
    val cryptoStore: ByteSize?,
)
