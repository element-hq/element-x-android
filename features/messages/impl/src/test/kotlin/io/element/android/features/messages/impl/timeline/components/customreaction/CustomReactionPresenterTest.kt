/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.components.customreaction

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.aTimelineItemReactions
import io.element.android.libraries.emoji.api.recentemojis.EmptyGetRecentEmojis
import io.element.android.libraries.emoji.test.fakeEmojiPickerPresenterFactory
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class CustomReactionPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    private val presenter = CustomReactionPresenter(
        emojiPickerPresenterFactory = fakeEmojiPickerPresenterFactory(),
        getRecentEmojis = EmptyGetRecentEmojis,
    )

    @Test
    fun `present - handle selecting and de-selecting an event`() = runTest {
        presenter.test {
            val event = aTimelineItemEvent(eventId = AN_EVENT_ID)
            val initialState = awaitItem()
            assertThat(initialState.target).isEqualTo(CustomReactionState.Target.None)

            initialState.eventSink(CustomReactionEvent.ShowCustomReactionSheet(event))

            val eventId = (awaitItem().target as? CustomReactionState.Target.Success)?.event?.eventId
            assertThat(eventId).isEqualTo(AN_EVENT_ID)

            initialState.eventSink(CustomReactionEvent.DismissCustomReactionSheet)
            assertThat(awaitItem().target).isEqualTo(CustomReactionState.Target.None)
        }
    }

    @Test
    fun `present - handle selected emojis`() = runTest {
        presenter.test {
            val reactions = aTimelineItemReactions(count = 1, isHighlighted = true)
            val event = aTimelineItemEvent(eventId = AN_EVENT_ID, timelineItemReactions = reactions)
            val initialState = awaitItem()
            assertThat(initialState.target).isEqualTo(CustomReactionState.Target.None)

            val key = reactions.reactions.first().key
            initialState.eventSink(CustomReactionEvent.ShowCustomReactionSheet(event))

            val stateWithSelectedEmojis = awaitItem()
            val eventId = (stateWithSelectedEmojis.target as? CustomReactionState.Target.Success)?.event?.eventId
            assertThat(eventId).isEqualTo(AN_EVENT_ID)
            assertThat(stateWithSelectedEmojis.selectedEmoji).contains(key)
        }
    }
}
