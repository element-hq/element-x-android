/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.test

import io.element.android.libraries.emoji.api.picker.EmojiPickerState

/**
 * Minimal [EmojiPickerState] for use in tests that don't render the picker (typically presenter
 * tests).
 */
data class FakeEmojiPickerState(
    override val isReady: Boolean,
) : EmojiPickerState
