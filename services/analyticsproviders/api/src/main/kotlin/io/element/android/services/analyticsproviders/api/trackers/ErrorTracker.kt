/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analyticsproviders.api.trackers

/**
 * Reports non-fatal errors to the crash reporting backend.
 */
interface ErrorTracker {
    /**
     * Records an error that was handled but is still worth knowing about.
     *
     * @param throwable the failure to report; never pass one whose message can contain user content.
     */
    fun trackError(throwable: Throwable)
}
