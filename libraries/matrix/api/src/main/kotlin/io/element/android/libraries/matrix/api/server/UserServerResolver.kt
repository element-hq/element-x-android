/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.server

/**
 * Resolves the server name of the logged in user, which is the part of their user id after the colon.
 */
interface UserServerResolver {
    /** Returns the server name, for instance `matrix.org` for `@alice:matrix.org`. */
    fun resolve(): String
}
