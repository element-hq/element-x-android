/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.api

import io.element.android.libraries.architecture.AsyncData

/** Status row shown by Settings -> Notifications ("Private notifications (ntfy)"). */
data class PrivatePushStatusState(
    val status: AsyncData<PrivatePushStatus>,
    val eventSink: (PrivatePushStatusEvents) -> Unit,
)

sealed interface PrivatePushStatusEvents {
    data object Refresh : PrivatePushStatusEvents
}
