/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.selection

import com.google.common.truth.Truth.assertThat
import io.element.android.features.messages.impl.timeline.aTimelineItemEvent
import io.element.android.features.messages.impl.timeline.components.event.aGalleryItem
import io.element.android.features.messages.impl.timeline.components.event.aTimelineItemGalleryContent
import io.element.android.features.messages.impl.timeline.model.event.GalleryItem
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.aTimelineItemTextContent
import io.element.android.libraries.matrix.api.core.EventId
import org.junit.Test

class CanSaveSelectionTest {
    @Test
    fun `empty selection cannot be saved`() {
        val items = listOf(aTimelineItemEvent(eventId = EventId("\$1"), content = aTimelineItemImageContent()))
        assertThat(canSaveSelection(items, emptySet())).isFalse()
    }

    @Test
    fun `selection of media can be saved, oldest first`() {
        val items = listOf(
            aTimelineItemEvent(eventId = EventId("\$1"), content = aTimelineItemImageContent(filename = "photo.jpg")).copy(sentTimeMillis = 1000L),
            aTimelineItemEvent(eventId = EventId("\$2"), content = aTimelineItemFileContent(fileName = "notes.pdf")).copy(sentTimeMillis = 2000L),
        )
        val media = savableSelection(items, setOf(EventId("\$1"), EventId("\$2")))
        assertThat(media.map { it.filename }).containsExactly("photo.jpg", "notes.pdf").inOrder()
    }

    @Test
    fun `selection containing a text message cannot be saved`() {
        val items = listOf(
            aTimelineItemEvent(eventId = EventId("\$1"), content = aTimelineItemImageContent()),
            aTimelineItemEvent(eventId = EventId("\$2"), content = aTimelineItemTextContent()),
        )
        assertThat(canSaveSelection(items, setOf(EventId("\$1"), EventId("\$2")))).isFalse()
    }

    @Test
    fun `selection with an event which is no longer loaded cannot be saved`() {
        val items = listOf(aTimelineItemEvent(eventId = EventId("\$1"), content = aTimelineItemImageContent()))
        assertThat(canSaveSelection(items, setOf(EventId("\$1"), EventId("\$2")))).isFalse()
    }

    @Test
    fun `a gallery counts as all of its items`() {
        val gallery = aTimelineItemGalleryContent(
            items = listOf(
                aGalleryItem(filename = "one.jpg"),
                aGalleryItem(filename = "two.mp4", type = GalleryItem.Type.Video),
            ),
        )
        val items = listOf(aTimelineItemEvent(eventId = EventId("\$1"), content = gallery))
        val media = savableSelection(items, setOf(EventId("\$1")))
        assertThat(media.map { it.filename }).containsExactly("one.jpg", "two.mp4")
    }
}
