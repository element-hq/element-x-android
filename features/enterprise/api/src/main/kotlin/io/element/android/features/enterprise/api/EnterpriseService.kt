/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.enterprise.api

import io.element.android.libraries.matrix.api.core.SessionId

interface EnterpriseService {
    val isEnterpriseBuild: Boolean
    suspend fun isEnterpriseUser(sessionId: SessionId): Boolean
}
