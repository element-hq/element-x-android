/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.messages.impl.pinned.banner

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.eventformatter.test.FakePinnedMessagesBannerFormatter
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.test.room.FakeMatrixRoom
import io.element.android.tests.testutils.test
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.Test

class PinnedMessagesBannerPresenterTest {
    @Test
    fun `present - initial state`() = runTest {
        val presenter = createPinnedMessagesBannerPresenter()
        presenter.test {
            val initialState = awaitItem()
            assertThat(initialState.pinnedMessagesCount).isEqualTo(0)
            assertThat(initialState.currentPinnedMessageIndex).isEqualTo(0)
            assertThat(initialState.currentPinnedMessage).isNull()
        }
    }

    @Test
    fun `present - move to next pinned message when there is no pinned events`() = runTest {
        val presenter = createPinnedMessagesBannerPresenter()
        presenter.test {
            val initialState = awaitItem()
            initialState.eventSink(PinnedMessagesBannerEvents.MoveToNextPinned)
            // Nothing is emitted
            ensureAllEventsConsumed()
        }
    }

    private fun TestScope.createPinnedMessagesBannerPresenter(
        room: MatrixRoom = FakeMatrixRoom(),
        itemFactory: PinnedMessagesBannerItemFactory = PinnedMessagesBannerItemFactory(
            coroutineDispatchers = testCoroutineDispatchers(),
            formatter = FakePinnedMessagesBannerFormatter(
                formatLambda = { event -> "Content ${event.content}" }
            )
        ),
        isFeatureEnabled: Boolean = true,
    ): PinnedMessagesBannerPresenter {
        val featureFlagService = FakeFeatureFlagService(
            initialState = mapOf(
                FeatureFlags.PinnedEvents.key to isFeatureEnabled
            )
        )
        return PinnedMessagesBannerPresenter(
            room = room,
            itemFactory = itemFactory,
            featureFlagService = featureFlagService
        )
    }
}
