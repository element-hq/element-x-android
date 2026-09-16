/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.api.pinned

import io.element.android.libraries.matrix.api.timeline.TimelineProvider

/**
 * A [TimelineProvider] whose active timeline holds the pinned events of the room, rather than its live events.
 *
 * It exists as its own type so that it can be injected where only the pinned timeline is wanted.
 */
interface PinnedEventsTimelineProvider : TimelineProvider
