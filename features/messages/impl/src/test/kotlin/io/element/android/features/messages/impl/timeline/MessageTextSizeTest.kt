/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline

import com.google.common.truth.Truth.assertThat
import io.element.android.compound.tokens.generated.TypographyTokens
import io.element.android.libraries.preferences.api.store.MessageTextSize
import org.junit.Test

/**
 * [MessageTextSize] lives in a module that cannot see the design system, so its values are literals. These guard them: the timeline scales its text
 * relative to the default, so if the two drift apart the slider stops showing the size it actually renders.
 *
 * [TypographyTokens] is generated from the Compound tokens, so it can change without anyone editing it here.
 */
class MessageTextSizeTest {
    @Test
    fun `the default size is the size the timeline renders message bodies at`() {
        assertThat(MessageTextSize.DEFAULT.toFloat()).isEqualTo(TypographyTokens.fontBodyLgRegular.fontSize.value)
    }

    @Test
    fun `the default size is inside the adjustable range`() {
        assertThat(MessageTextSize.range).contains(MessageTextSize.DEFAULT)
    }

    @Test
    fun `the default size renders the timeline unscaled`() {
        assertThat(MessageTextSize.scaleOf(MessageTextSize.DEFAULT)).isEqualTo(1f)
    }
}
