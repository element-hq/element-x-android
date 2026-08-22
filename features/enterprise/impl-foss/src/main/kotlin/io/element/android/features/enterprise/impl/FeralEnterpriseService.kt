/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Copyright (c) 2026 Feral / feralisme.fr
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.enterprise.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.enterprise.api.EnterpriseService
import java.util.Locale

/**
 * Feral members-only [EnterpriseService].
 *
 * Restores the Feral homeserver restriction (members-only lock) inside the module that is
 * ACTUALLY compiled for the public/FOSS build, `:features:enterprise:impl-foss`, by REPLACING
 * the upstream [DefaultEnterpriseService] binding through Metro's `replaces`.
 *
 * Only the two members that implement the lock are overridden; everything else is delegated
 * to [DefaultEnterpriseService] so that future upstream additions to the interface keep their
 * FOSS defaults without touching this file. Upstream never edits this file, so a rebase/merge
 * cannot silently revert it. Guarded by `FeralEnterpriseServiceTest`.
 * See `docs/FERAL_MAINTENANCE.md`.
 */
@ContributesBinding(AppScope::class, replaces = [DefaultEnterpriseService::class])
class FeralEnterpriseService : EnterpriseService by DefaultEnterpriseService() {
    /**
     * A Feral regional Matrix homeserver the app is allowed to connect to.
     * `locales` maps ISO language/country codes to this server for locale-based
     * pre-selection in onboarding.
     */
    data class FeralServer(
        val url: String,
        val description: String,
        val locales: Set<String> = emptySet(),
    )

    /**
     * The ONLY homeservers a Feral build may connect to. This is the members-only allow-list.
     *
     * Verified 2026-08-21: `feralism.net` resolves to the same VPS but does NOT serve Matrix
     * (404 on /_matrix/client/versions, no well-known), so it must not be offered in onboarding.
     * Re-add it here if it ever becomes a real homeserver.
     */
    private val feralServers = listOf(
        FeralServer(
            url = "https://feralisme.fr",
            description = "Serveur France",
            locales = setOf("fr", "FR"),
        ),
        // FeralServer(
        //     url = "https://feralism.net",
        //     description = "International server",
        // ),
    )

    override fun homeserverAllowList(): List<String> {
        val default = defaultHomeserverForLocale()
        return listOf(default) + feralServers.map { it.url }.filter { it != default }
    }

    override suspend fun isAllowedToConnectToHomeserver(homeserverUrl: String): Boolean {
        val normalized = normalize(homeserverUrl)
        return feralServers.any { normalized == normalize(it.url) }
    }

    private fun defaultHomeserverForLocale(): String {
        val locale = Locale.getDefault()
        return feralServers.firstOrNull { server ->
            server.locales.contains(locale.country) || server.locales.contains(locale.language)
        }?.url ?: "https://feralisme.fr"
    }

    private fun normalize(url: String): String {
        val trimmed = url.trim().removeSuffix("/")
        return when {
            trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("http://") -> "https://" + trimmed.removePrefix("http://")
            else -> "https://$trimmed"
        }
    }
}
