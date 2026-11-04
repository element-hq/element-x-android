/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.paths

/**
 * Delete the session directories (files and cache) which are not used anymore, i.e. which are not referenced by any
 * stored session nor by a client currently in use. Such directories can be left over, for instance, when the application
 * is killed during a login flow.
 */
interface UnusedSessionPathsCleaner {
    suspend fun clean()
}
