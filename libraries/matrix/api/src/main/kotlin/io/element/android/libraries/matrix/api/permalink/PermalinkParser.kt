/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.permalink

/**
 * This class turns a uri to a [PermalinkData].
 * element-based domains (e.g. https://app.element.io/#/user/@chagai95:matrix.org) permalinks
 * or matrix.to permalinks (e.g. https://matrix.to/#/@chagai95:matrix.org)
 * or client permalinks (e.g. <clientPermalinkBaseUrl>user/@chagai95:matrix.org)
 * or matrix: permalinks (e.g. matrix:u/chagai95:matrix.org)
 */
interface PermalinkParser {
    /**
     * Turns a uri string to a [PermalinkData].
     * https://github.com/matrix-org/matrix-doc/blob/master/proposals/1704-matrix.to-permalinks.md
     */
    fun parse(uriString: String): PermalinkData
}
