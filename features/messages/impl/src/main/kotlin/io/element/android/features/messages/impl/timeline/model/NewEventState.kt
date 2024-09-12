/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.timeline.model

/**
 * Model if there is a new event in the timeline and if it is from me or from other.
 * This can be used to scroll to the bottom of the list when a new event is added.
 */
enum class NewEventState {
    None,
    FromMe,
    FromOther
}
