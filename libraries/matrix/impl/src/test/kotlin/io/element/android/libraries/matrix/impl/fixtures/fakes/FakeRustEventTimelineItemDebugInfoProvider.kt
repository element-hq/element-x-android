/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.impl.fixtures.factories.anEventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfoProvider
import org.matrix.rustcomponents.sdk.NoPointer

class FakeRustEventTimelineItemDebugInfoProvider(
    private val debugInfo: EventTimelineItemDebugInfo = anEventTimelineItemDebugInfo(),
) : EventTimelineItemDebugInfoProvider(NoPointer) {
    override fun get(): EventTimelineItemDebugInfo = debugInfo
}
