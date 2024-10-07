/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.viewfolder.test.file

import io.element.android.features.viewfolder.impl.file.FileSave

class FakeFileSave : FileSave {
    var hasBeenCalled = false
        private set

    override suspend fun save(path: String) {
        hasBeenCalled = true
    }
}
