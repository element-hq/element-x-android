/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.reporter

import okhttp3.HttpUrl

/**
 * The folder where the log files are written, depending on the bug report configuration of all the signed-in accounts.
 * Since the log files are shared by all the accounts, they are stored per bug report server, so that the logs
 * written while an account using a server is signed in can never be sent to another server.
 */
sealed interface LogFolder {
    /**
     * All the accounts use the default bug report server: the logs are written in the root log folder.
     */
    data object Default : LogFolder

    /**
     * All the accounts use the same custom bug report server: the logs are written in a folder dedicated to this server.
     */
    data class Server(val name: String) : LogFolder

    /**
     * Bug reporting is disabled for at least one account, or the accounts use different bug report servers:
     * the logs are written in a folder which is never sent.
     */
    data object LocalOnly : LogFolder
}

private const val LOCAL_ONLY_FOLDER_NAME = "local-only"

/**
 * @return the name of the subfolder of the root log folder, or null for the root log folder.
 */
fun LogFolder.subfolderName(): String? = when (this) {
    LogFolder.Default -> null
    is LogFolder.Server -> name
    LogFolder.LocalOnly -> LOCAL_ONLY_FOLDER_NAME
}

/**
 * Compute the log folder from the bug report URLs of all the signed-in accounts.
 *
 * @param urls the bug report URL of each account, null when bug reporting is disabled for this account.
 * @param defaultUrl the default bug report URL, null if there is none.
 */
internal fun computeLogFolder(urls: List<HttpUrl?>, defaultUrl: HttpUrl?): LogFolder {
    val distinctUrls = urls.distinct()
    if (distinctUrls.size != 1) {
        // Different configurations
        return LogFolder.LocalOnly
    }
    // Note: if there is no default URL, the default configuration and disabled bug reports cannot be distinguished,
    // and the logs are written to the root log folder. There is no bug report server to send them to anyway.
    return when (val url = distinctUrls.first()) {
        defaultUrl -> LogFolder.Default
        null -> LogFolder.LocalOnly
        else -> LogFolder.Server(url.toFolderName())
    }
}

/**
 * Sanitise the URL to get a folder name, for instance `https://rageshakes.example.org/api/submit` gives `rageshakes.example.org_api_submit`.
 */
internal fun HttpUrl.toFolderName(): String {
    val portSuffix = if (port == HttpUrl.defaultPort(scheme)) "" else "_$port"
    val path = encodedPath.trim('/')
    return buildString {
        append(host)
        append(portSuffix)
        if (path.isNotEmpty()) {
            append('_')
            append(path)
        }
    }.replace(Regex("[^A-Za-z0-9.-]"), "_")
}
