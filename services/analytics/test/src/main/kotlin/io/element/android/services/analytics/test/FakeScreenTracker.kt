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

package io.element.android.services.analytics.test

import androidx.compose.runtime.Composable
import im.vector.app.features.analytics.plan.MobileScreen
import io.element.android.services.analytics.api.ScreenTracker
import io.element.android.tests.testutils.lambda.lambdaError

class FakeScreenTracker(
    private val trackScreenLambda: (MobileScreen.ScreenName) -> Unit = { lambdaError() }
) : ScreenTracker {
    @Composable
    override fun TrackScreen(screen: MobileScreen.ScreenName) {
        trackScreenLambda(screen)
    }
}
