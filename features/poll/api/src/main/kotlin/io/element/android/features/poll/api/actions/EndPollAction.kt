/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.poll.api.actions

import io.element.android.libraries.matrix.api.core.EventId

interface EndPollAction {
    suspend fun execute(pollStartId: EventId): Result<Unit>
}
