/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.server

import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.server.UserServerResolver

@ContributesBinding(SessionScope::class)
class DefaultUserServerResolver(
    private val matrixClient: MatrixClient,
) : UserServerResolver {
    override fun resolve(): String {
        return matrixClient.userIdServerName()
    }
}
