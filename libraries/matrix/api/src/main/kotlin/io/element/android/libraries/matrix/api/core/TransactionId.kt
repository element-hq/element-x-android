/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import java.io.Serializable

@JvmInline
value class TransactionId(val value: String) : Serializable {
    override fun toString(): String = value
}
