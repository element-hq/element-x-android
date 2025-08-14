/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.test

import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.features.enterprise.api.BugReportUrl
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeEnterpriseService(
    override val isEnterpriseBuild: Boolean = false,
    private val isEnterpriseUserResult: (SessionId) -> Boolean = { lambdaError() },
    private val defaultHomeserverListResult: () -> List<String> = { emptyList() },
    private val isAllowedToConnectToHomeserverResult: (String) -> Boolean = { lambdaError() },
    private val semanticColorsLightResult: () -> SemanticColors = { lambdaError() },
    private val semanticColorsDarkResult: () -> SemanticColors = { lambdaError() },
    private val firebasePushGatewayResult: () -> String? = { lambdaError() },
    private val unifiedPushDefaultPushGatewayResult: () -> String? = { lambdaError() },
) : EnterpriseService {
    override suspend fun isEnterpriseUser(sessionId: SessionId): Boolean = simulateLongTask {
        isEnterpriseUserResult(sessionId)
    }

    override fun defaultHomeserverList(): List<String> {
        return defaultHomeserverListResult()
    }

    override suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String): Boolean = simulateLongTask {
        isAllowedToConnectToHomeserverResult(homeserverUrl)
    }

    override fun semanticColorsLight(): SemanticColors {
        return semanticColorsLightResult()
    }

    override fun semanticColorsDark(): SemanticColors {
        return semanticColorsDarkResult()
    }

    override fun firebasePushGateway(): String? {
        return firebasePushGatewayResult()
    }

    override fun unifiedPushDefaultPushGateway(): String? {
        return unifiedPushDefaultPushGatewayResult()
    }

    val bugReportUrlMutableFlow = MutableStateFlow<BugReportUrl>(BugReportUrl.UseDefault)
    override val bugReportUrlFlow: Flow<BugReportUrl> = bugReportUrlMutableFlow.asStateFlow()
}
