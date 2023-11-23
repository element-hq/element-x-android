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

package io.element.android.features.messages.impl.timeline.model

import org.junit.Assert.assertEquals
import org.junit.Test

class AggregatedReactionTest {
    @Test
    fun `reaction display key is shortened`() {
        val reaction = anAggregatedReaction(
            key = "1234567890123456790",
            count = 1
        )

        assertEquals("1234567890123456…", reaction.displayKey)
    }

    @Test
    fun `reaction count and isHighlighted are computed correctly`() {
        val reaction = anAggregatedReaction(
            key = "1234567890123456790",
            count = 3,
            isHighlighted = true
        )

        assertEquals(3, reaction.count)
        assertEquals(true, reaction.isHighlighted)
    }
}
