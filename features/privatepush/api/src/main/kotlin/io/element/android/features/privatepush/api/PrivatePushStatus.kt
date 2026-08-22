/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.privatepush.api

import androidx.compose.runtime.Immutable

@Immutable
sealed interface PrivatePushStatus {
    /** ntfy distributor registered and the endpoint is on the Feral server. */
    data object Private : PrivatePushStatus

    /** Registered, but the endpoint lives on another server (e.g. ntfy.sh). */
    data class PublicServer(val host: String) : PrivatePushStatus

    data class NotSetUp(val reason: Reason) : PrivatePushStatus {
        enum class Reason {
            NtfyNotInstalled,
            NotConnected,
        }
    }
}
