/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.api

import androidx.compose.ui.graphics.Color
import io.element.android.compound.colors.SemanticColorsLightDark
import io.element.android.libraries.matrix.api.ClientUrlContentFetcher
import io.element.android.libraries.matrix.api.core.SessionId
import kotlinx.coroutines.flow.Flow

/**
 * Exposes the restrictions and customisations an enterprise deployment can impose: allowed homeservers, branding, push gateways and more.
 *
 * On a standard build this reports permissive defaults, so callers do not need to branch on the build type themselves.
 */
interface EnterpriseService {
    /**
     * Whether the given session belongs to an enterprise deployment, which can be true even on a standard build.
     *
     * @param sessionId the session to check.
     */
    suspend fun isEnterpriseUser(sessionId: SessionId): Boolean

    /**
     * Rewrites the authentication server URL when the deployment requires a different one from the one advertised.
     *
     * @param url the URL to rewrite.
     * @param urlContentFetcher used to read the deployment configuration; a client that is not authenticated yet also works.
     */
    suspend fun tweakMasUrl(url: String, urlContentFetcher: ClientUrlContentFetcher): String

    /**
     * Returns the list of homeservers the user is allowed to sign in to.
     *
     * If the list is empty or contains the special value [ANY_ACCOUNT_PROVIDER], the user is allowed to sign in to any homeserver.
     */
    fun homeserverAllowList(): List<String>

    /**
     * Whether the user is allowed to sign in to a given homeserver, according to [homeserverAllowList].
     *
     * @param homeserverUrl the server the user is trying to use.
     */
    suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String): Boolean

    /**
     * Whether the given homeserver enforces the use of Element Pro or a derived app.
     *
     * @param serverName the homeserver to check.
     */
    suspend fun isElementProEnforced(serverName: String): Boolean

    /**
     * Override the brand color.
     * @param sessionId the session to override the brand color for, or null to set the brand color to use when there is no session.
     * @param brandColor the color in hex format (#RRGGBBAA or #RRGGBB), or null to reset to default.
     */
    suspend fun overrideBrandColor(sessionId: SessionId?, brandColor: String?)

    /**
     * The brand color to apply, or `null` when the default theme color should be used.
     *
     * @param sessionId the session to read the color of, or `null` for the color used when no session is active.
     */
    fun brandColorsFlow(sessionId: SessionId?): Flow<Color?>

    /**
     * The full light and dark color palettes derived from the brand color.
     *
     * @param sessionId the session to read the colors of, or `null` for the colors used when no session is active.
     */
    fun semanticColorsFlow(sessionId: SessionId?): Flow<SemanticColorsLightDark>

    /** The push gateway to register with when using Firebase, or `null` to use the app default. */
    fun firebasePushGateway(): String?

    /** The push gateway to register with when using UnifiedPush, or `null` to use the app default. */
    fun unifiedPushDefaultPushGateway(): String?

    /**
     * Where bug reports should be submitted, which an enterprise deployment can redirect or disable entirely.
     *
     * @param sessionId the session to read the setting of, or `null` for the setting used when no session is active.
     */
    fun bugReportUrlFlow(sessionId: SessionId?): Flow<BugReportUrl>

    /**
     * Gets Notification Channel to use for the noisy notifications of the provided session.
     * Returns `null` when the session has no dedicated channel and the app-wide one should be used.
     *
     * @param sessionId the session whose channel is requested.
     */
    fun getNoisyNotificationChannelId(sessionId: SessionId): String?

    companion object {
        const val ANY_ACCOUNT_PROVIDER = "*"
    }
}

fun EnterpriseService.canConnectToAnyHomeserver(): Boolean {
    return homeserverAllowList().let {
        it.isEmpty() || it.contains(EnterpriseService.ANY_ACCOUNT_PROVIDER)
    }
}
