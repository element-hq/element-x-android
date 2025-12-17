/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.timeline

import io.element.android.libraries.matrix.api.timeline.ReceiptType
import org.matrix.rustcomponents.sdk.ReceiptType as RustReceiptType

internal fun ReceiptType.toRustReceiptType(): RustReceiptType = when (this) {
    ReceiptType.READ -> RustReceiptType.READ
    ReceiptType.READ_PRIVATE -> RustReceiptType.READ_PRIVATE
    ReceiptType.FULLY_READ -> RustReceiptType.FULLY_READ
}
