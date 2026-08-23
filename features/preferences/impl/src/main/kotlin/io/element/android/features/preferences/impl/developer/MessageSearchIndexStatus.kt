/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer

/**
 * What the "Message search index" developer-settings section shows.
 *
 * Derived from four signals: the feature flag (visibility), index availability (the index only
 * attaches at client build, so a mid-session flag flip needs a restart), the user-initiated
 * sweep's WorkManager activity, and the durable sweep cursor (progress numbers).
 *
 * [Finished] means the sweep drained its room queue — deliberately not "everything is searchable":
 * history already present in the local event-cache store is skipped upstream of the app, so a
 * completeness claim cannot be justified. Wording in the UI must stay "swept/fetched".
 */
sealed interface MessageSearchIndexStatus {
    /** Feature flag off — the section is not rendered at all. */
    data object Hidden : MessageSearchIndexStatus

    /** Flag ticked but the index is not attached to this session yet — restart required. */
    data object RestartNeeded : MessageSearchIndexStatus

    /** No sweep recorded yet — offer "Start indexing". */
    data object Idle : MessageSearchIndexStatus

    /** A previous sweep stopped mid-queue and no work is active — offer to resume. */
    data class Paused(val roomsDone: Int, val roomsTotal: Int) : MessageSearchIndexStatus

    /** The user's sweep is enqueued but not executing — typically waiting for a connection. */
    data object WaitingForRun : MessageSearchIndexStatus

    /** The user's sweep is executing right now. Zero [roomsTotal] means it is still preparing. */
    data class Running(val roomsDone: Int, val roomsTotal: Int) : MessageSearchIndexStatus

    /** The last sweep drained its queue. */
    data class Finished(val roomsSwept: Int, val pagesFetched: Int) : MessageSearchIndexStatus
}
