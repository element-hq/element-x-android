/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.model

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.AN_EVENT_ID_2
import io.element.android.libraries.matrix.test.AN_EVENT_ID_3
import kotlinx.collections.immutable.persistentListOf
import org.junit.Test

class GroupedMediaItemsTest {
    @Test
    fun `hasEvent returns the expected value`() {
        val sut = GroupedMediaItems(
            imageAndVideoItems = persistentListOf(
                aMediaItemImage(eventId = AN_EVENT_ID),
            ),
            fileItems = persistentListOf(
                aMediaItemAudio(eventId = AN_EVENT_ID_2),
            ),
        )
        assertThat(sut.hasEvent(AN_EVENT_ID)).isTrue()
        assertThat(sut.hasEvent(AN_EVENT_ID_2)).isTrue()
        assertThat(sut.hasEvent(AN_EVENT_ID_3)).isFalse()
    }
}
