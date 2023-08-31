/*
 * Copyright (c) 2021 New Vector Ltd
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

package io.element.android.features.preferences.impl.about

import androidx.annotation.StringRes
import io.element.android.libraries.ui.strings.CommonStrings

private const val COPYRIGHT_URL = "https://element.io/copyright"
private const val USE_POLICY_URL = "https://element.io/acceptable-use-policy-terms"
private const val PRIVACY_URL = "https://element.io/privacy"

sealed class ElementLegal(
    @StringRes val titleRes: Int,
    val url: String,
) {
    data object Copyright : ElementLegal(CommonStrings.common_copyright, COPYRIGHT_URL)
    data object AcceptableUsePolicy : ElementLegal(CommonStrings.common_acceptable_use_policy, USE_POLICY_URL)
    data object PrivacyPolicy : ElementLegal(CommonStrings.common_privacy_policy, PRIVACY_URL)
}

fun getAllLegals(): List<ElementLegal> {
    return listOf(
        ElementLegal.Copyright,
        ElementLegal.AcceptableUsePolicy,
        ElementLegal.PrivacyPolicy,
    )
}
