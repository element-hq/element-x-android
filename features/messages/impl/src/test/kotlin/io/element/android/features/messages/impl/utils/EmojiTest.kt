/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiTest {
    @Test
    fun validEmojis() {
        // Simple single/multiple single-codepoint emojis per string
        assertTrue("👍".containsOnlyEmojis())
        assertTrue("😀".containsOnlyEmojis())
        assertTrue("🙂🙁".containsOnlyEmojis())
        assertTrue("👁❤️🍝".containsOnlyEmojis()) // 👁 is a pictographic
        assertTrue("👨‍👩‍👦1️⃣🚀👳🏾‍♂️🪩".containsOnlyEmojis())
        assertTrue("🌍🌎🌏".containsOnlyEmojis())

        // Awkward multi-codepoint graphemes
        assertTrue("🧑‍🧑‍🧒‍🧒".containsOnlyEmojis())
        assertTrue("🏴‍☠".containsOnlyEmojis())
        assertTrue("👩🏿‍🔧".containsOnlyEmojis())

        Assert.assertFalse("".containsOnlyEmojis())
        Assert.assertFalse(" ".containsOnlyEmojis())
        Assert.assertFalse("🙂 🙁".containsOnlyEmojis())
        Assert.assertFalse(" 🙂 🙁 ".containsOnlyEmojis())
        Assert.assertFalse("Hello".containsOnlyEmojis())
        Assert.assertFalse("Hello 👋".containsOnlyEmojis())
    }
}
