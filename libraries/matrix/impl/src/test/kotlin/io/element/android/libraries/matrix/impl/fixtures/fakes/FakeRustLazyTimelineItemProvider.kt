/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.impl.fixtures.factories.anEventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.LazyTimelineItemProvider
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.ShieldState

class FakeRustLazyTimelineItemProvider(
    private val debugInfo: EventTimelineItemDebugInfo = anEventTimelineItemDebugInfo(),
    private val shieldsState: ShieldState? = null,
) : LazyTimelineItemProvider(NoPointer) {
    override fun getShields(strict: Boolean) = shieldsState
    override fun debugInfo() = debugInfo
}
