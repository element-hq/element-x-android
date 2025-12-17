/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.analytics.impl.store

import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.MutableStateFlow

internal class FakeAnalyticsStore(
    defaultUserConsent: Boolean = false,
    defaultDidAskUserConsent: Boolean = false,
    defaultAnalyticsId: String = "",
    private val resetLambda: () -> Unit = { lambdaError() },
) : AnalyticsStore {
    override val userConsentFlow = MutableStateFlow(defaultUserConsent)
    override val didAskUserConsentFlow = MutableStateFlow(defaultDidAskUserConsent)
    override val analyticsIdFlow = MutableStateFlow(defaultAnalyticsId)

    override suspend fun setUserConsent(newUserConsent: Boolean) {
        userConsentFlow.emit(newUserConsent)
    }

    override suspend fun setDidAskUserConsent(newValue: Boolean) {
        didAskUserConsentFlow.emit(newValue)
    }

    override suspend fun setAnalyticsId(newAnalyticsId: String) {
        analyticsIdFlow.emit(newAnalyticsId)
    }

    override suspend fun reset() {
        resetLambda()
    }
}
