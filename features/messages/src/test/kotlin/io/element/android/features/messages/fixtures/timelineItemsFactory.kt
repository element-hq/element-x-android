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

package io.element.android.features.messages.fixtures

import io.element.android.features.messages.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentFailedToParseMessageFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentFailedToParseStateFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentMessageFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentProfileChangeFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentRedactedFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentRoomMembershipFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentStateFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentStickerFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemContentUTDFactory
import io.element.android.features.messages.timeline.factories.event.TimelineItemEventFactory
import io.element.android.features.messages.timeline.factories.virtual.TimelineItemDaySeparatorFactory
import io.element.android.features.messages.timeline.factories.virtual.TimelineItemVirtualFactory
import io.element.android.libraries.dateformatter.test.FakeDaySeparatorFormatter

internal fun aTimelineItemsFactory() = TimelineItemsFactory(
    dispatchers = testCoroutineDispatchers(),
    eventItemFactory = TimelineItemEventFactory(
        TimelineItemContentFactory(
            messageFactory = TimelineItemContentMessageFactory(),
            redactedMessageFactory = TimelineItemContentRedactedFactory(),
            stickerFactory = TimelineItemContentStickerFactory(),
            utdFactory = TimelineItemContentUTDFactory(),
            roomMembershipFactory = TimelineItemContentRoomMembershipFactory(),
            profileChangeFactory = TimelineItemContentProfileChangeFactory(),
            stateFactory = TimelineItemContentStateFactory(),
            failedToParseMessageFactory = TimelineItemContentFailedToParseMessageFactory(),
            failedToParseStateFactory = TimelineItemContentFailedToParseStateFactory()
        )
    ),
    virtualItemFactory = TimelineItemVirtualFactory(
        daySeparatorFactory = TimelineItemDaySeparatorFactory(
            FakeDaySeparatorFormatter()
        ),
    )
)
