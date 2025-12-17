/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.appconfig

object RageshakeConfig {
    /**
     * The URL to submit bug reports to.
     */
    const val BUG_REPORT_URL = BuildConfig.BUG_REPORT_URL

    /**
     * As per https://github.com/matrix-org/rageshake:
     * Identifier for the application (eg 'riot-web').
     * Should correspond to a mapping configured in the configuration file for github issue reporting to work.
     */
    const val BUG_REPORT_APP_NAME = BuildConfig.BUG_REPORT_APP_NAME

    /**
     * The maximum size of the upload request. Default value is just below CloudFlare's max request size.
     */
    const val MAX_LOG_UPLOAD_SIZE = 50 * 1024 * 1024L
}
