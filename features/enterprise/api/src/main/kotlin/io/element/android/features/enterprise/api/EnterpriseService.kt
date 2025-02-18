/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.libraries.matrix.api.core.SessionId

interface EnterpriseService {
    val isEnterpriseBuild: Boolean
    suspend fun isEnterpriseUser(sessionId: SessionId): Boolean

    fun semanticColorsLight(): SemanticColors
    fun semanticColorsDark(): SemanticColors
}
