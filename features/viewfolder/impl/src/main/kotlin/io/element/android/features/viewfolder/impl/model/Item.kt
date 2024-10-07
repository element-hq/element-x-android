/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.model

import androidx.compose.runtime.Immutable

@Immutable
sealed interface Item {
    data object Parent : Item

    data class Folder(
        val path: String,
        val name: String,
    ) : Item

    data class File(
        val path: String,
        val name: String,
        val formattedSize: String,
    ) : Item
}
