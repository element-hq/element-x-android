/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.widget

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
class MatrixWidgetSettings(
    val id: String,
    val initAfterContentLoad: Boolean,
    val rawUrl: String,
) : Parcelable {
    companion object
}
