/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId

data class ShareState(
    val shareAction: AsyncAction<ShareResult>,
    val eventSink: (ShareEvent) -> Unit
)

/**
 * The result of a successful share.
 * @param sessionId the session which has been used to share the data, it can be another session than the current one.
 * @param roomIds the rooms the data has been shared to.
 */
data class ShareResult(
    val sessionId: SessionId,
    val roomIds: List<RoomId>,
)
