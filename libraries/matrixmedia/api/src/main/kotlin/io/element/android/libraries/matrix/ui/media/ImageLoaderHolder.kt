/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.media

import coil3.ImageLoader
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.SessionId

/**
 * Caches one Coil image loader per session, since loading Matrix media requires the session's own client to fetch and decrypt it.
 */
interface ImageLoaderHolder {
    /** Returns a loader that can only fetch ordinary URLs, for images that do not come from a Matrix room. */
    fun get(): ImageLoader

    /**
     * Returns the loader of a session, creating it on first use.
     *
     * @param client the session whose media the loader will fetch.
     */
    fun get(client: MatrixClient): ImageLoader

    /**
     * Drops the cached loader of a session, to be called when that session goes away.
     *
     * @param sessionId the session whose loader is dropped.
     */
    fun remove(sessionId: SessionId)
}
