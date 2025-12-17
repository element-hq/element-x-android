/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package extension

import com.android.build.api.dsl.VariantDimension

fun VariantDimension.buildConfigFieldStr(
    name: String,
    value: String,
) {
    buildConfigField(
        type = "String",
        name = name,
        value = "\"$value\""
    )
}

fun VariantDimension.buildConfigFieldBoolean(
    name: String,
    value: Boolean,
) {
    buildConfigField(
        type = "boolean",
        name = name,
        value = value.toString()
    )
}
