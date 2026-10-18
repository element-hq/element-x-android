/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.emoji.api.picker

/**
 * Opaque state produced by an [EmojiPickerPresenter] and consumed by an [EmojiPickerRenderer].
 */
interface EmojiPickerState {
    /**
     * `true` once the picker has finished loading its data and is safe to display.
     * Callers should typically defer showing the picker (or its container, e.g. a bottom sheet)
     * until this flips to `true` to avoid flashing an empty picker.
     */
    val isReady: Boolean
}
