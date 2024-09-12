/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.appconfig

object RageshakeConfig {
    /**
     * The URL to submit bug reports to.
     */
    const val BUG_REPORT_URL = "https://riot.im/bugreports/submit"

    /**
     * As per https://github.com/matrix-org/rageshake:
     * Identifier for the application (eg 'riot-web').
     * Should correspond to a mapping configured in the configuration file for github issue reporting to work.
     */
    const val BUG_REPORT_APP_NAME = "element-x-android"

    /**
     * The maximum size of the upload request. Default value is just below CloudFlare's max request size.
     */
    const val MAX_LOG_UPLOAD_SIZE = 50 * 1024 * 1024L
}
