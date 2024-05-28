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

package io.element.android.services.analyticsproviders.test

import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.UserProperties

import io.element.android.services.analyticsproviders.api.AnalyticsProvider
import io.element.android.tests.testutils.lambda.lambdaError

class FakeAnalyticsProvider(
    override val name: String = "FakeAnalyticsProvider",
    private val initLambda: () -> Unit = { lambdaError() },
    private val stopLambda: () -> Unit = { lambdaError() },
    private val captureLambda: (VectorAnalyticsEvent) -> Unit = { lambdaError() },
    private val screenLambda: (VectorAnalyticsScreen) -> Unit = { lambdaError() },
    private val updateUserPropertiesLambda: (UserProperties) -> Unit = { lambdaError() },
    private val trackErrorLambda: (Throwable) -> Unit = { lambdaError() }
) : AnalyticsProvider {
    override fun init() = initLambda()
    override fun stop() = stopLambda()
    override fun capture(event: VectorAnalyticsEvent) = captureLambda(event)
    override fun screen(screen: VectorAnalyticsScreen) = screenLambda(screen)
    override fun updateUserProperties(userProperties: UserProperties) = updateUserPropertiesLambda(userProperties)
    override fun trackError(throwable: Throwable) = trackErrorLambda(throwable)
}
