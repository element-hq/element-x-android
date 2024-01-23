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

package io.element.android.features.location.impl.common.actions

import io.element.android.features.location.api.Location

class FakeLocationActions : LocationActions {
    var sharedLocation: Location? = null
        private set

    var sharedLabel: String? = null
        private set

    var openSettingsInvocationsCount = 0
        private set

    override fun share(location: Location, label: String?) {
        sharedLocation = location
        sharedLabel = label
    }

    override fun openSettings() {
        openSettingsInvocationsCount++
    }
}
