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

package io.element.android.features.messages.impl.fixtures

import io.element.android.features.messages.impl.timeline.TimelineItemIndexer
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentFailedToParseMessageFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentFailedToParseStateFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentMessageFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentPollFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentProfileChangeFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentRedactedFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentRoomMembershipFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentStateFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentStickerFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemContentUTDFactory
import io.element.android.features.messages.impl.timeline.factories.event.TimelineItemEventFactory
import io.element.android.features.messages.impl.timeline.factories.virtual.TimelineItemDaySeparatorFactory
import io.element.android.features.messages.impl.timeline.factories.virtual.TimelineItemVirtualFactory
import io.element.android.features.messages.impl.timeline.groups.TimelineItemGrouper
import io.element.android.features.messages.impl.utils.FakeTextPillificationHelper
import io.element.android.features.messages.test.timeline.FakeHtmlConverterProvider
import io.element.android.features.poll.test.pollcontent.FakePollContentStateFactory
import io.element.android.libraries.androidutils.filesize.FakeFileSizeFormatter
import io.element.android.libraries.dateformatter.test.FakeDaySeparatorFormatter
import io.element.android.libraries.dateformatter.test.FakeLastMessageTimestampFormatter
import io.element.android.libraries.eventformatter.api.TimelineEventFormatter
import io.element.android.libraries.featureflag.test.FakeFeatureFlagService
import io.element.android.libraries.matrix.api.timeline.item.event.EventTimelineItem
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.mediaviewer.api.util.FileExtensionExtractorWithoutValidation
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope

internal fun TestScope.aTimelineItemsFactory(
    timelineItemIndexer: TimelineItemIndexer = TimelineItemIndexer()
): TimelineItemsFactory {
    val timelineEventFormatter = aTimelineEventFormatter()
    val matrixClient = FakeMatrixClient()
    return TimelineItemsFactory(
        dispatchers = testCoroutineDispatchers(),
        eventItemFactory = TimelineItemEventFactory(
            contentFactory = TimelineItemContentFactory(
                messageFactory = TimelineItemContentMessageFactory(
                    fileSizeFormatter = FakeFileSizeFormatter(),
                    fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
                    featureFlagService = FakeFeatureFlagService(),
                    htmlConverterProvider = FakeHtmlConverterProvider(),
                    permalinkParser = FakePermalinkParser(),
                    textPillificationHelper = FakeTextPillificationHelper(),
                ),
                redactedMessageFactory = TimelineItemContentRedactedFactory(),
                stickerFactory = TimelineItemContentStickerFactory(
                    fileSizeFormatter = FakeFileSizeFormatter(),
                    fileExtensionExtractor = FileExtensionExtractorWithoutValidation()
                ),
                pollFactory = TimelineItemContentPollFactory(FakeFeatureFlagService(), FakePollContentStateFactory()),
                utdFactory = TimelineItemContentUTDFactory(),
                roomMembershipFactory = TimelineItemContentRoomMembershipFactory(timelineEventFormatter),
                profileChangeFactory = TimelineItemContentProfileChangeFactory(timelineEventFormatter),
                stateFactory = TimelineItemContentStateFactory(timelineEventFormatter),
                failedToParseMessageFactory = TimelineItemContentFailedToParseMessageFactory(),
                failedToParseStateFactory = TimelineItemContentFailedToParseStateFactory(),
            ),
            matrixClient = matrixClient,
            lastMessageTimestampFormatter = FakeLastMessageTimestampFormatter(),
            permalinkParser = FakePermalinkParser(),
        ),
        virtualItemFactory = TimelineItemVirtualFactory(
            daySeparatorFactory = TimelineItemDaySeparatorFactory(
                FakeDaySeparatorFormatter()
            ),
        ),
        timelineItemGrouper = TimelineItemGrouper(),
        timelineItemIndexer = timelineItemIndexer,
    )
}

internal fun aTimelineEventFormatter(): TimelineEventFormatter {
    return object : TimelineEventFormatter {
        override fun format(event: EventTimelineItem): CharSequence {
            return ""
        }
    }
}
