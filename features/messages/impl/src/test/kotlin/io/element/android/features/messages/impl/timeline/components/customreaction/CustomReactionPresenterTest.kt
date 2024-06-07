/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.tests.testutils.WarmUpRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CustomReactionPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val presenter = CustomReactionPresenter(emojibaseProvider = FakeEmojibaseProvider())

    @Test
    fun `present - handle selecting and de-selecting an event`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val event = aTimelineItemEvent(eventId = AN_EVENT_ID)
            val initialState = awaitItem()
            assertThat(initialState.target).isEqualTo(CustomReactionState.Target.None)

            initialState.eventSink(CustomReactionEvents.ShowCustomReactionSheet(event))

            assertThat(awaitItem().target).isEqualTo(CustomReactionState.Target.Loading(event))

            val eventId = (awaitItem().target as? CustomReactionState.Target.Success)?.event?.eventId
            assertThat(eventId).isEqualTo(AN_EVENT_ID)

            initialState.eventSink(CustomReactionEvents.DismissCustomReactionSheet)
            assertThat(awaitItem().target).isEqualTo(CustomReactionState.Target.None)
        }
    }

    @Test
    fun `present - handle selected emojis`() = runTest {
        moleculeFlow(RecompositionMode.Immediate) {
            presenter.present()
        }.test {
            val reactions = aTimelineItemReactions(count = 1, isHighlighted = true)
            val event = aTimelineItemEvent(eventId = AN_EVENT_ID, timelineItemReactions = reactions)
            val initialState = awaitItem()
            assertThat(initialState.target).isEqualTo(CustomReactionState.Target.None)

            val key = reactions.reactions.first().key
            initialState.eventSink(CustomReactionEvents.ShowCustomReactionSheet(event))

            assertThat(awaitItem().target).isEqualTo(CustomReactionState.Target.Loading(event))

            val stateWithSelectedEmojis = awaitItem()
            val eventId = (stateWithSelectedEmojis.target as? CustomReactionState.Target.Success)?.event?.eventId
            assertThat(eventId).isEqualTo(AN_EVENT_ID)
            assertThat(stateWithSelectedEmojis.selectedEmoji).contains(key)
        }
    }
}
