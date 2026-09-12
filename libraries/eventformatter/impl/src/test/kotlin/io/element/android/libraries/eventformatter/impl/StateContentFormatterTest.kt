/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.eventformatter.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.eventformatter.impl.mode.RenderingMode
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.services.toolbox.impl.strings.AndroidStringProvider
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class StateContentFormatterTest : RobolectricTest() {
    private val formatter = StateContentFormatter(
        AndroidStringProvider(RuntimeEnvironment.getApplication().resources)
    )

    @Test
    fun `a custom state event is not rendered in the timeline`() {
        assertThat(format(RenderingMode.Timeline)).isNull()
    }

    @Test
    fun `a custom state event is not rendered in the room list`() {
        assertThat(format(RenderingMode.RoomList)).isNull()
    }

    private fun format(renderingMode: RenderingMode) = formatter.format(
        stateContent = StateContent(stateKey = "", content = OtherState.Custom("com.example.custom")),
        senderDisambiguatedDisplayName = "Alice",
        senderIsYou = false,
        renderingMode = renderingMode,
    )
}
