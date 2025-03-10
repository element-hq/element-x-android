/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.test

import io.element.android.compound.tokens.generated.SemanticColors
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.tests.testutils.simulateLongTask

class FakeEnterpriseService(
    override val isEnterpriseBuild: Boolean = false,
    private val isEnterpriseUserResult: (SessionId) -> Boolean = { lambdaError() },
    private val defaultHomeserverResult: () -> String? = { A_FAKE_HOMESERVER },
    private val semanticColorsLightResult: () -> SemanticColors = { lambdaError() },
    private val semanticColorsDarkResult: () -> SemanticColors = { lambdaError() },
) : EnterpriseService {
    override suspend fun isEnterpriseUser(sessionId: SessionId): Boolean = simulateLongTask {
        isEnterpriseUserResult(sessionId)
    }

    override fun defaultHomeserver(): String? {
        return defaultHomeserverResult()
    }

    override fun semanticColorsLight(): SemanticColors {
        return semanticColorsLightResult()
    }

    override fun semanticColorsDark(): SemanticColors {
        return semanticColorsDarkResult()
    }

    companion object {
        const val A_FAKE_HOMESERVER = "a_fake_homeserver"
    }
}
