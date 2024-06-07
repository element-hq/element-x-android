/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.services.analytics.impl.store

import io.element.android.tests.testutils.lambda.lambdaError
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAnalyticsStore(
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
