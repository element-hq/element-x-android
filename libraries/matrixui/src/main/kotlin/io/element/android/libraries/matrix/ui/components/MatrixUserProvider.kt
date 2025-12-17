/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser

open class MatrixUserProvider : PreviewParameterProvider<MatrixUser> {
    override val values: Sequence<MatrixUser>
        get() = sequenceOf(
            aMatrixUser(),
            aMatrixUser(displayName = null),
        )
}

open class MatrixUserWithNullProvider : PreviewParameterProvider<MatrixUser?> {
    override val values: Sequence<MatrixUser?>
        get() = sequenceOf(
            aMatrixUser(),
            aMatrixUser(displayName = null),
            null,
        )
}

open class MatrixUserWithAvatarProvider : PreviewParameterProvider<MatrixUser?> {
    override val values: Sequence<MatrixUser?>
        get() = sequenceOf(
            aMatrixUser(displayName = "John Doe"),
            aMatrixUser(displayName = "John Doe", avatarUrl = "anUrl"),
        )
}

fun aMatrixUser(
    id: String = "@id_of_alice:server.org",
    displayName: String? = "Alice",
    avatarUrl: String? = null,
) = MatrixUser(
    userId = UserId(id),
    displayName = displayName,
    avatarUrl = avatarUrl,
)

fun aMatrixUserList() = listOf(
    aMatrixUser("@alice:server.org", "Alice"),
    aMatrixUser("@bob:server.org", "Bob"),
    aMatrixUser("@carol:server.org", "Carol"),
    aMatrixUser("@david:server.org", "David"),
    aMatrixUser("@eve:server.org", "Eve"),
    aMatrixUser("@justin:server.org", "Justin"),
    aMatrixUser("@mallory:server.org", "Mallory"),
    aMatrixUser("@susie:server.org", "Susie"),
    aMatrixUser("@victor:server.org", "Victor"),
    aMatrixUser("@walter:server.org", "Walter"),
)
