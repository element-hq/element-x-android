/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.protection

fun aTimelineProtectionState(
    protectionState: ProtectionState = ProtectionState.RenderAll,
    eventSink: (TimelineProtectionEvent) -> Unit = {},
) = TimelineProtectionState(
    protectionState = protectionState,
    eventSink = eventSink,
)
