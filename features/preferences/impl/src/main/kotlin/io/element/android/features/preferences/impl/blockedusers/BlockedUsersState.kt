/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.blockedusers

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.ImmutableList

data class BlockedUsersState(
    val isDebugBuild: Boolean,
    val blockedUsers: ImmutableList<MatrixUser>,
    val unblockUserAction: AsyncAction<Unit>,
    val eventSink: (BlockedUsersEvents) -> Unit,
)
