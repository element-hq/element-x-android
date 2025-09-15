/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.leave

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.spaces.SpaceRoom
import kotlinx.collections.immutable.ImmutableList

data class ConfirmingLeavingSpace(
    val spaceName: String?,
    val roomsWhereUserIsTheOnlyAdmin: AsyncData<ImmutableList<SpaceRoom>>,
) : AsyncAction.Confirming
