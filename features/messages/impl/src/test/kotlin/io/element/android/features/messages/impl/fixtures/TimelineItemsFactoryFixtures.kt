/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.fixtures

import io.element.android.features.messages.impl.messagesummary.FakeMessageSummaryFormatter
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactory
import io.element.android.features.messages.impl.timeline.factories.TimelineItemsFactoryConfig
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
import io.element.android.libraries.dateformatter.test.FakeDateFormatter
import io.element.android.libraries.eventformatter.api.TimelineEventFormatter
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.timeline.item.event.EventContent
import io.element.android.libraries.matrix.test.FakeMatrixClient
import io.element.android.libraries.matrix.test.permalink.FakePermalinkParser
import io.element.android.libraries.mediaviewer.test.util.FileExtensionExtractorWithoutValidation
import io.element.android.tests.testutils.testCoroutineDispatchers
import kotlinx.coroutines.test.TestScope

internal fun TestScope.aTimelineItemsFactoryCreator(): TimelineItemsFactory.Creator {
    return object : TimelineItemsFactory.Creator {
        override fun create(config: TimelineItemsFactoryConfig): TimelineItemsFactory {
            return aTimelineItemsFactory(config)
        }
    }
}

internal fun TestScope.aTimelineItemsFactory(
    config: TimelineItemsFactoryConfig,
): TimelineItemsFactory {
    val timelineEventFormatter = aTimelineEventFormatter()
    val matrixClient = FakeMatrixClient()
    return TimelineItemsFactory(
        dispatchers = testCoroutineDispatchers(),
        eventItemFactoryCreator = object : TimelineItemEventFactory.Creator {
            override fun create(config: TimelineItemsFactoryConfig): TimelineItemEventFactory {
                return TimelineItemEventFactory(
                    contentFactory = TimelineItemContentFactory(
                        messageFactory = TimelineItemContentMessageFactory(
                            fileSizeFormatter = FakeFileSizeFormatter(),
                            fileExtensionExtractor = FileExtensionExtractorWithoutValidation(),
                            htmlConverterProvider = FakeHtmlConverterProvider(),
                            permalinkParser = FakePermalinkParser(),
                            textPillificationHelper = FakeTextPillificationHelper(),
                        ),
                        redactedMessageFactory = TimelineItemContentRedactedFactory(),
                        stickerFactory = TimelineItemContentStickerFactory(
                            fileSizeFormatter = FakeFileSizeFormatter(),
                            fileExtensionExtractor = FileExtensionExtractorWithoutValidation()
                        ),
                        pollFactory = TimelineItemContentPollFactory(FakePollContentStateFactory()),
                        utdFactory = TimelineItemContentUTDFactory(),
                        roomMembershipFactory = TimelineItemContentRoomMembershipFactory(timelineEventFormatter),
                        profileChangeFactory = TimelineItemContentProfileChangeFactory(timelineEventFormatter),
                        stateFactory = TimelineItemContentStateFactory(timelineEventFormatter),
                        failedToParseMessageFactory = TimelineItemContentFailedToParseMessageFactory(),
                        failedToParseStateFactory = TimelineItemContentFailedToParseStateFactory(),
                        sessionId = matrixClient.sessionId,
                    ),
                    matrixClient = matrixClient,
                    dateFormatter = FakeDateFormatter(),
                    permalinkParser = FakePermalinkParser(),
                    config = config,
                    summaryFormatter = FakeMessageSummaryFormatter(),
                )
            }
        },
        virtualItemFactory = TimelineItemVirtualFactory(
            daySeparatorFactory = TimelineItemDaySeparatorFactory(
                FakeDateFormatter()
            ),
        ),
        timelineItemGrouper = TimelineItemGrouper(),
        config = config
    )
}

internal fun aTimelineEventFormatter(): TimelineEventFormatter {
    return object : TimelineEventFormatter {
        override fun format(content: EventContent, isOutgoing: Boolean, sender: UserId, senderDisambiguatedDisplayName: String): CharSequence? {
            return ""
        }
    }
}
