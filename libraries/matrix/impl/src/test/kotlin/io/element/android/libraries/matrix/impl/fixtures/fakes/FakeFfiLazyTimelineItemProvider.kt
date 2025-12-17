/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.impl.fixtures.factories.anEventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.LazyTimelineItemProvider
import org.matrix.rustcomponents.sdk.NoHandle
import org.matrix.rustcomponents.sdk.SendHandle
import org.matrix.rustcomponents.sdk.ShieldState

class FakeFfiLazyTimelineItemProvider(
    private val debugInfo: EventTimelineItemDebugInfo = anEventTimelineItemDebugInfo(),
    private val shieldsState: ShieldState? = null,
) : LazyTimelineItemProvider(NoHandle) {
    override fun getShields(strict: Boolean) = shieldsState
    override fun debugInfo() = debugInfo
    override fun getSendHandle(): SendHandle? = null
}
