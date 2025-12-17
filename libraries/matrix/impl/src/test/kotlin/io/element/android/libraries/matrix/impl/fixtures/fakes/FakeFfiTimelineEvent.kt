/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.impl.fixtures.factories.aRustTimelineEventTypeMessageLike
import io.element.android.libraries.matrix.test.A_USER_ID_2
import io.element.android.services.toolbox.test.systemclock.A_FAKE_TIMESTAMP
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.TimelineEvent
import org.matrix.rustcomponents.sdk.TimelineEventType

open class FakeFfiTimelineEvent(
    val timestamp: ULong = A_FAKE_TIMESTAMP.toULong(),
    val timelineEventType: TimelineEventType = aRustTimelineEventTypeMessageLike(),
    val senderId: String = A_USER_ID_2.value,
) : TimelineEvent(NoHandle) {
    override fun timestamp(): ULong = timestamp
    override fun eventType(): TimelineEventType = timelineEventType
    override fun senderId(): String = senderId
}
