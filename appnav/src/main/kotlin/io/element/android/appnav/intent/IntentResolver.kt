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

package io.element.android.appnav.intent

import android.content.Intent
import io.element.android.features.login.api.oidc.OidcAction
import io.element.android.features.login.api.oidc.OidcIntentResolver
import io.element.android.libraries.deeplink.DeeplinkData
import io.element.android.libraries.deeplink.DeeplinkParser
import timber.log.Timber
import javax.inject.Inject

sealed interface ResolvedIntent {
    data class Navigation(val deeplinkData: DeeplinkData) : ResolvedIntent
    data class Oidc(val oidcAction: OidcAction) : ResolvedIntent
}

class IntentResolver @Inject constructor(
    private val deeplinkParser: DeeplinkParser,
    private val oidcIntentResolver: OidcIntentResolver
) {
    fun resolve(intent: Intent): ResolvedIntent? {
        if (intent.canBeIgnored()) return null

        val deepLinkData = deeplinkParser.getFromIntent(intent)
        if (deepLinkData != null) return ResolvedIntent.Navigation(deepLinkData)

        val oidcAction = oidcIntentResolver.resolve(intent)
        if (oidcAction != null) return ResolvedIntent.Oidc(oidcAction)

        // Unknown intent
        Timber.w("Unknown intent")
        return null
    }
}

private fun Intent.canBeIgnored(): Boolean {
    return action == Intent.ACTION_MAIN &&
        categories?.contains(Intent.CATEGORY_LAUNCHER) == true
}
