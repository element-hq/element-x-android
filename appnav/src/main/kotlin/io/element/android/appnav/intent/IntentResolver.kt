/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appnav.intent

import android.content.Intent
import dev.zacsweers.metro.Inject
import io.element.android.features.login.api.LoginIntentResolver
import io.element.android.features.login.api.LoginParams
import io.element.android.libraries.deeplink.api.DeeplinkData
import io.element.android.libraries.deeplink.api.DeeplinkParser
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.oidc.api.OidcAction
import io.element.android.libraries.oidc.api.OidcIntentResolver
import timber.log.Timber

sealed interface ResolvedIntent {
    data class Navigation(val deeplinkData: DeeplinkData) : ResolvedIntent
    data class Oidc(val oidcAction: OidcAction) : ResolvedIntent
    data class Permalink(val permalinkData: PermalinkData) : ResolvedIntent
    data class Login(val params: LoginParams) : ResolvedIntent
    data class IncomingShare(val intent: Intent) : ResolvedIntent
}

@Inject
class IntentResolver(
    private val deeplinkParser: DeeplinkParser,
    private val loginIntentResolver: LoginIntentResolver,
    private val oidcIntentResolver: OidcIntentResolver,
    private val permalinkParser: PermalinkParser,
) {
    fun resolve(intent: Intent): ResolvedIntent? {
        if (intent.canBeIgnored()) return null

        // Coming from a notification?
        val deepLinkData = deeplinkParser.getFromIntent(intent)
        if (deepLinkData != null) return ResolvedIntent.Navigation(deepLinkData)

        // Coming during login using Oidc?
        val oidcAction = oidcIntentResolver.resolve(intent)
        if (oidcAction != null) return ResolvedIntent.Oidc(oidcAction)

        val actionViewData = intent
            .takeIf { it.action == Intent.ACTION_VIEW }
            ?.dataString

        // Mobile configuration link clicked? (mobile.element.io)
        val mobileLoginData = actionViewData
            ?.let { loginIntentResolver.parse(it) }
        if (mobileLoginData != null) return ResolvedIntent.Login(mobileLoginData)

        // External link clicked? (matrix.to, element.io, etc.)
        val permalinkData = actionViewData
            ?.let { permalinkParser.parse(it) }
            ?.takeIf { it !is PermalinkData.FallbackLink }
        if (permalinkData != null) return ResolvedIntent.Permalink(permalinkData)

        if (intent.action == Intent.ACTION_SEND || intent.action == Intent.ACTION_SEND_MULTIPLE) {
            return ResolvedIntent.IncomingShare(intent)
        }

        // Unknown intent
        Timber.w("Unknown intent")
        return null
    }
}

private fun Intent.canBeIgnored(): Boolean {
    return action == Intent.ACTION_MAIN &&
        categories?.contains(Intent.CATEGORY_LAUNCHER) == true
}
