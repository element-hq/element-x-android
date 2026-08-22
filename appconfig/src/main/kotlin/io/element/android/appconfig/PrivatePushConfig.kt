/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

/**
 * Feral private notifications ("private push").
 *
 * Feral ships without Google services; notifications are delivered by the Feral ntfy server
 * (UnifiedPush server + Matrix push gateway) through the ntfy helper app installed on the phone.
 * The in-app setup flow (features/privatepush) installs/configures ntfy and verifies that the
 * registered endpoint really lives on [SERVER_HOST].
 */
object PrivatePushConfig {
    const val ENABLED: Boolean = true

    /** Address the member pastes into ntfy > Settings > General > Default server. */
    const val SERVER_URL: String = "https://ntfy.feralisme.fr"

    /** Host an endpoint must have to count as private. */
    const val SERVER_HOST: String = "ntfy.feralisme.fr"

    /** The ntfy Android app (UnifiedPush distributor). */
    const val NTFY_PACKAGE: String = "io.heckel.ntfy"

    /** Public manifest describing the ntfy APK mirrored on feralisme.fr. */
    const val NTFY_MANIFEST_URL: String = "https://feralisme.fr/media/downloads/android/ntfy.json"

    /**
     * SHA-256 (DER, lowercase hex) of the ntfy release signing certificate.
     * Pinned here: the manifest value is only cross-checked, never trusted on its own.
     */
    const val NTFY_SIGNING_CERT_SHA256: String = "6e145d7ae685eff75468e5067e03a6c3645453343e4e181dac8b6b17ff67489d"

    const val PLAY_STORE_PACKAGE: String = "com.android.vending"
    val FDROID_PACKAGES: List<String> = listOf("org.fdroid.fdroid", "org.fdroid.basic")

    /** Web fallback when the F-Droid app cannot open the market:// link. */
    const val NTFY_FDROID_WEB_URL: String = "https://f-droid.org/packages//"

    /** Poll interval while the Install page waits for ntfy to appear. */
    const val INSTALL_POLL_INTERVAL_MS: Long = 2_000L
}
