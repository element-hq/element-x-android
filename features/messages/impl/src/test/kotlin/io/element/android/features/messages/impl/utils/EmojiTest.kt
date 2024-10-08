/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.utils

import org.junit.Assert
import org.junit.Assert.assertTrue
import org.junit.Test

class EmojiTest {
    @Test
    fun validEmojis() {
        // Simple single/multiple single-codepoint emojis per string
        assertTrue("👍".containsOnlyEmojisInternal())
        assertTrue("😀".containsOnlyEmojisInternal())
        assertTrue("🙂🙁".containsOnlyEmojisInternal())
        assertTrue("👁❤️🍝".containsOnlyEmojisInternal()) // 👁 is a pictographic
        assertTrue("👨‍👩‍👦1️⃣🚀👳🏾‍♂️🪩".containsOnlyEmojisInternal())
        assertTrue("🌍🌎🌏".containsOnlyEmojisInternal())

        // Awkward multi-codepoint graphemes
        assertTrue("🧑‍🧑‍🧒‍🧒".containsOnlyEmojisInternal())
        assertTrue("🏴‍☠".containsOnlyEmojisInternal())
        assertTrue("👩🏿‍🔧".containsOnlyEmojisInternal())

        Assert.assertFalse("".containsOnlyEmojisInternal())
        Assert.assertFalse(" ".containsOnlyEmojisInternal())
        Assert.assertFalse("🙂 🙁".containsOnlyEmojisInternal())
        Assert.assertFalse(" 🙂 🙁 ".containsOnlyEmojisInternal())
        Assert.assertFalse("Hello".containsOnlyEmojisInternal())
        Assert.assertFalse("Hello 👋".containsOnlyEmojisInternal())
    }
}
