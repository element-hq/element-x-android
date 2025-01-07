/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.user

import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId
import javax.inject.Inject

@SingleIn(SessionScope::class)
class CurrentSessionIdHolder @Inject constructor(matrixClient: MatrixClient) {
    val current = matrixClient.sessionId

    fun isCurrentSession(sessionId: SessionId?): Boolean = current == sessionId
}
