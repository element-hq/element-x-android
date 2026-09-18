/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.event

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class TimelineItemCircleViewTest {
    @Test
    fun `play size fills the column minus the progress ring`() {
        assertThat(circlePlayMediaSize(281f)).isEqualTo(271f)
        assertThat(circlePlayMediaSize(Float.POSITIVE_INFINITY)).isEqualTo(240f)
    }

    @Test
    fun `idle size is 75 percent of play`() {
        assertThat(circleIdleMediaSize(271f)).isEqualTo(203.25f)
        assertThat(circleIdleMediaSize(240f)).isEqualTo(180f)
    }
}
