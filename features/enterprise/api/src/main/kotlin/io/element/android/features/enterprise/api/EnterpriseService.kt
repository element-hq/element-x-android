/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

import androidx.compose.ui.graphics.Color
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow

interface EnterpriseService {
    val isEnterpriseBuild: Boolean
    suspend fun isEnterpriseUser(sessionId: SessionId): Boolean
    fun defaultHomeserverList(): List<String>
    suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String): Boolean

    /**
     * Override the brand color.
     * @param sessionId the session to override the brand color for, or null to set the brand color to use when there is no session.
     * @param brandColor the color in hex format (#RRGGBBAA or #RRGGBB), or null to reset to default.
     */
    suspend fun overrideBrandColor(sessionId: SessionId?, brandColor: String?)

    fun brandColorsFlow(sessionId: SessionId?): Flow<Color?>

    fun semanticColorsFlow(sessionId: SessionId?): Flow<SemanticColorsLightDark>

    fun firebasePushGateway(): String?
    fun unifiedPushDefaultPushGateway(): String?

    fun bugReportUrlFlow(sessionId: SessionId?): Flow<BugReportUrl>

    companion object {
        const val ANY_ACCOUNT_PROVIDER = "*"
    }
}

fun EnterpriseService.canConnectToAnyHomeserver(): Boolean {
    return defaultHomeserverList().let {
        it.isEmpty() || it.contains(EnterpriseService.ANY_ACCOUNT_PROVIDER)
    }
}
