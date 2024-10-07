/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.viewfolder.impl.folder

import io.element.android.features.viewfolder.impl.model.Item
import kotlinx.collections.immutable.ImmutableList

data class ViewFolderState(
    val path: String,
    val content: ImmutableList<Item>,
)
