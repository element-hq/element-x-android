/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ReadReceiptBottomSheetPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `present - handle event selected`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent()
            initialState.eventSink(ReadReceiptBottomSheetEvents.EventSelected(selectedEvent))
            assertThat(awaitItem().selectedEvent).isSameInstanceAs(selectedEvent)
        }
    }

    @Test
    fun `present - handle dismiss`() = runTest {
        val presenter = createPresenter()
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val initialState = awaitItem()
            val selectedEvent = aTimelineItemEvent()
            initialState.eventSink(ReadReceiptBottomSheetEvents.EventSelected(selectedEvent))
            skipItems(1)
            initialState.eventSink(ReadReceiptBottomSheetEvents.Dismiss)
            assertThat(awaitItem().selectedEvent).isNull()
        }
    }

    private fun createPresenter() = ReadReceiptBottomSheetPresenter(buildMeta = aBuildMeta())
}
