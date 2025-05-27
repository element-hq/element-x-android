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
    fun defaultHomeserverList(): List<String>
    suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String): Boolean

    suspend fun isElementCallAvailable(): Boolean

    fun semanticColorsLight(): SemanticColors
    fun semanticColorsDark(): SemanticColors

    fun firebasePushGateway(): String?
    fun unifiedPushDefaultPushGateway(): String?

    companion object {
        const val ANY_ACCOUNT_PROVIDER = "*"
    }
}

fun EnterpriseService.canConnectToAnyHomeserver(): Boolean {
    return defaultHomeserverList().let {
        it.isEmpty() || it.contains(EnterpriseService.ANY_ACCOUNT_PROVIDER)
    }
}
