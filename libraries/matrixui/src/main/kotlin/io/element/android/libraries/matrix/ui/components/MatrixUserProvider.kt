/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.libraries.matrix.ui.components

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser

open class MatrixUserProvider : PreviewParameterProvider<MatrixUser> {
    override val values: Sequence<MatrixUser>
        get() = sequenceOf(
            aMatrixUser(),
            aMatrixUser().copy(displayName = null),
        )
}

fun aMatrixUser(id: String = "@id_of_alice:server.org", displayName: String = "Alice") = MatrixUser(
    userId = UserId(id),
    displayName = displayName,
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

open class MatrixUserWithNullProvider : PreviewParameterProvider<MatrixUser?> {
    override val values: Sequence<MatrixUser?>
        get() = sequenceOf(
            aMatrixUser(),
            aMatrixUser().copy(displayName = null),
            null,
        )
}
