/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.Message
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.TimelineItemContent
import org.matrix.rustcomponents.sdk.TimelineItemContentKind

class FakeRustTimelineItemContent : TimelineItemContent(NoPointer) {
    override fun asMessage(): Message? = null
    override fun kind(): TimelineItemContentKind = TimelineItemContentKind.Message
}
