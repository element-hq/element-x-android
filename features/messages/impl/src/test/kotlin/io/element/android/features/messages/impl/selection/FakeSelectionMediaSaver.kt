/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

class FakeSelectionMediaSaver(
    private val failFor: (SavableMedia) -> Boolean = { false },
) : SelectionMediaSaver {
    val savedFilenames = mutableListOf<String>()

    override suspend fun save(media: SavableMedia): Result<Unit> {
        if (failFor(media)) return Result.failure(RuntimeException("Save failed"))
        savedFilenames.add(media.filename)
        return Result.success(Unit)
    }
}
