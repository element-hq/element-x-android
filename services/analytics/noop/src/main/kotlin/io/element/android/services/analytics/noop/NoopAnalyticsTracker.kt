/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.services.analytics.noop

import com.squareup.anvil.annotations.ContributesBinding
import im.vector.app.features.analytics.itf.VectorAnalyticsEvent
import im.vector.app.features.analytics.itf.VectorAnalyticsScreen
import im.vector.app.features.analytics.plan.UserProperties
import io.element.android.libraries.di.AppScope
import io.element.android.services.analytics.api.AnalyticsTracker
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class NoopAnalyticsTracker @Inject constructor() : AnalyticsTracker {

    override fun capture(event: VectorAnalyticsEvent) = Unit

    override fun screen(screen: VectorAnalyticsScreen) = Unit

    override fun updateUserProperties(userProperties: UserProperties) = Unit
}
