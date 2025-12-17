/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.blockedusers

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Inject
class BlockedUsersPresenter(
    private val matrixClient: MatrixClient,
    private val featureFlagService: FeatureFlagService,
) : Presenter<BlockedUsersState> {
    @Composable
    override fun present(): BlockedUsersState {
        val coroutineScope = rememberCoroutineScope()

        var pendingUserToUnblock by remember {
            mutableStateOf<UserId?>(null)
        }
        val unblockUserAction: MutableState<AsyncAction<Unit>> = remember {
            mutableStateOf(AsyncAction.Uninitialized)
        }

        val renderBlockedUsersDetail by remember {
            featureFlagService.isFeatureEnabledFlow(FeatureFlags.ShowBlockedUsersDetails)
        }.collectAsState(initial = false)
        val ignoredUserIds by matrixClient.ignoredUsersFlow.collectAsState()
        val ignoredMatrixUser by produceState(
            initialValue = ignoredUserIds.map { MatrixUser(userId = it) },
            key1 = renderBlockedUsersDetail,
            key2 = ignoredUserIds
        ) {
            value = ignoredUserIds.map {
                if (renderBlockedUsersDetail) {
                    matrixClient.getProfile(it).getOrNull()
                } else {
                    null
                }
                    ?: MatrixUser(userId = it)
            }
        }

        fun handleEvent(event: BlockedUsersEvents) {
            when (event) {
                is BlockedUsersEvents.Unblock -> {
                    pendingUserToUnblock = event.userId
                    unblockUserAction.value = AsyncAction.ConfirmingNoParams
                }
                BlockedUsersEvents.ConfirmUnblock -> {
                    pendingUserToUnblock?.let {
                        coroutineScope.unblockUser(it, unblockUserAction)
                        pendingUserToUnblock = null
                    }
                }
                BlockedUsersEvents.Cancel -> {
                    pendingUserToUnblock = null
                    unblockUserAction.value = AsyncAction.Uninitialized
                }
            }
        }
        return BlockedUsersState(
            blockedUsers = ignoredMatrixUser.toImmutableList(),
            unblockUserAction = unblockUserAction.value,
            eventSink = ::handleEvent,
        )
    }

    private fun CoroutineScope.unblockUser(userId: UserId, asyncAction: MutableState<AsyncAction<Unit>>) = launch {
        runUpdatingState(asyncAction) {
            matrixClient.unignoreUser(userId)
        }
    }
}
