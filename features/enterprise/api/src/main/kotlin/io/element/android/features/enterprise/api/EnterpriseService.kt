/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow

interface EnterpriseService {
    val isEnterpriseBuild: Boolean
    suspend fun isEnterpriseUser(sessionId: SessionId): Boolean
    fun defaultHomeserverList(): List<String>
    suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String): Boolean

    /**
     * Override the brand color.
     * @param brandColor the color in hex format (#RRGGBBAA or #RRGGBB), or null to reset to default.
     */
    fun overrideBrandColor(brandColor: String?)

    @Composable
    fun semanticColorsLight(): State<SemanticColors>

    @Composable
    fun semanticColorsDark(): State<SemanticColors>

    fun firebasePushGateway(): String?
    fun unifiedPushDefaultPushGateway(): String?

    val bugReportUrlFlow: Flow<BugReportUrl>

    companion object {
        const val ANY_ACCOUNT_PROVIDER = "*"
    }
}

fun EnterpriseService.canConnectToAnyHomeserver(): Boolean {
    return defaultHomeserverList().let {
        it.isEmpty() || it.contains(EnterpriseService.ANY_ACCOUNT_PROVIDER)
    }
}
