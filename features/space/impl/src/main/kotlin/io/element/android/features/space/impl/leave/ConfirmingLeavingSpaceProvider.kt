/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import io.element.android.libraries.previewutils.room.aSpaceRoom
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

class ConfirmingLeavingSpaceProvider : PreviewParameterProvider<ConfirmingLeavingSpace> {
    override val values: Sequence<ConfirmingLeavingSpace>
        get() = sequenceOf(
            aConfirmingLeavingSpace(),
            aConfirmingLeavingSpace(
                roomsWhereUserIsTheOnlyAdmin = AsyncData.Success(persistentListOf()),
            ),
            aConfirmingLeavingSpace(
                spaceName = null,
                roomsWhereUserIsTheOnlyAdmin = AsyncData.Success(
                    persistentListOf(
                        aSpaceRoom(),
                        aSpaceRoom(
                            worldReadable = true,
                        ),
                        aSpaceRoom(
                            joinRule = JoinRule.Private,
                        ),
                    )
                )
            ),
        )
}

fun aConfirmingLeavingSpace(
    spaceName: String? = "Element Space",
    roomsWhereUserIsTheOnlyAdmin: AsyncData<ImmutableList<SpaceRoom>> = AsyncData.Uninitialized,
) = ConfirmingLeavingSpace(
    spaceName = spaceName,
    roomsWhereUserIsTheOnlyAdmin = roomsWhereUserIsTheOnlyAdmin,
)
