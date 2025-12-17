/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.viewfolder.test.folder

import io.element.android.features.viewfolder.impl.folder.FolderExplorer
import io.element.android.features.viewfolder.impl.model.Item

class FakeFolderExplorer : FolderExplorer {
    private var result: List<Item> = emptyList()

    fun givenResult(result: List<Item>) {
        this.result = result
    }

    override suspend fun getItems(path: String): List<Item> = result
}
