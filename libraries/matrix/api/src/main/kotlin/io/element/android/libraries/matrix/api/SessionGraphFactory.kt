/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

/**
 * Create the dependency injection graph of a session.
 * It can be used to get session scoped dependencies of a session which is not the current one.
 */
interface SessionGraphFactory {
    fun create(client: MatrixClient): Any
}
