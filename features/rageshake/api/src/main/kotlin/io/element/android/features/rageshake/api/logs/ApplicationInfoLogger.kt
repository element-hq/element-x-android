/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.api.logs

/**
 * Writes the application information (version, commits, local time) to the logs.
 * It is done at startup, and each time the log files start being written to another log folder,
 * so that the logs of every folder contain the information of the running application.
 */
fun interface ApplicationInfoLogger {
    fun log()
}
