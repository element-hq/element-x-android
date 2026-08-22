/*
 * Copyright (c) 2026 Element Creations Ltd.
 * Feral-owned file: written for the Feral fork, absent upstream (docs/FERAL_MAINTENANCE.md §4).
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

/**
 * Feral in-app updater configuration.
 *
 * Feral is distributed as a signed APK from feralisme.fr (no app store), so the app
 * checks this public endpoint for new releases and offers an in-app download+install.
 * Security model (see docs/FERAL_MAINTENANCE.md):
 *  - HTTPS transport, public manifest;
 *  - the downloaded APK is accepted ONLY if its signing certificate matches
 *    [SIGNING_CERT_SHA256] (the Feral release keystore) AND its versionCode is
 *    strictly greater than the installed one (anti-downgrade);
 *  - Android itself re-verifies the same-signer rule at install time.
 */
object AppUpdateConfig {
    const val ENABLED: Boolean = true

    /** Public base URL of the update channel (served by nginx, no auth). */
    const val BASE_URL: String = "https://feralisme.fr/media/downloads/android/"

    /** SHA-256 of the Feral release signing certificate (DER), lowercase hex. */
    const val SIGNING_CERT_SHA256: String =
        "574ad3f6a2dee26fb80314aa87c351ab4000d9c171a8dd0f79da2a854c00b578"

    /** Minimum delay between two update checks. */
    const val CHECK_INTERVAL_MS: Long = 6 * 60 * 60 * 1000L
}
