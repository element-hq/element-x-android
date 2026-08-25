/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.analytics

import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Reads the on-disk size of a session's SDK stores, so they can be reported as analytics.
 */
fun interface GetDatabaseSizesUseCase {
    /**
     * Returns the size of each store of the given session.
     *
     * @param sessionId the session whose stores are measured.
     */
    operator fun invoke(sessionId: SessionId): Result<SdkStoreSizes>
}
