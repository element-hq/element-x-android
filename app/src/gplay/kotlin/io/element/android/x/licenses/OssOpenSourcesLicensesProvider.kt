/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.x.licenses

import android.app.Activity
import android.content.Intent
import com.google.android.gms.oss.licenses.OssLicensesMenuActivity
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.preferences.api.OpenSourceLicensesProvider
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.ui.strings.CommonStrings
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class OssOpenSourcesLicensesProvider @Inject constructor() : OpenSourceLicensesProvider {
    override val hasOpenSourceLicenses: Boolean = true

    override fun navigateToOpenSourceLicenses(activity: Activity) {
        val title = activity.getString(CommonStrings.common_open_source_licenses)
        OssLicensesMenuActivity.setActivityTitle(title)
        activity.startActivity(Intent(activity, OssLicensesMenuActivity::class.java))
    }
}
