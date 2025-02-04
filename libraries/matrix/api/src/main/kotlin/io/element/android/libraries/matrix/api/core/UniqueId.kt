/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.core

import java.io.Serializable

@JvmInline
value class UniqueId(val value: String) : Serializable {
    override fun toString(): String = value
}
