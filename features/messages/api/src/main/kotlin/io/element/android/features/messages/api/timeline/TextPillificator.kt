/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.timeline

/**
 * Replaces Matrix ids, room aliases, `@room` and Matrix permalinks found in a text with mention pills.
 */
interface TextPillificator {
    fun pillify(text: CharSequence, pillifyPermalinks: Boolean = true): CharSequence
}
