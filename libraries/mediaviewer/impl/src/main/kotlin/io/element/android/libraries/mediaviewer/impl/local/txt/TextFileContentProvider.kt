/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.local.txt

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class TextFileContentProvider : PreviewParameterProvider<AsyncData<ImmutableList<String>>> {
    override val values: Sequence<AsyncData<ImmutableList<String>>>
        get() = sequenceOf(
            AsyncData.Uninitialized,
            AsyncData.Loading(),
            AsyncData.Success(persistentListOf("Hello, World!")),
            AsyncData.Failure(Exception("Failed to load text")),
        )
}
