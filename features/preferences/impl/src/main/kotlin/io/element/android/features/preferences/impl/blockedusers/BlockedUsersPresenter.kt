/*
 * Copyright (c) 2024 New Vector Ltd
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
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.architecture.runUpdatingState
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class BlockedUsersPresenter @Inject constructor(
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

        val renderBlockedUsersDetail = featureFlagService
            .isFeatureEnabledFlow(FeatureFlags.ShowBlockedUsersDetails)
            .collectAsState(initial = false)
        val ignoredUserIds by matrixClient.ignoredUsersFlow.collectAsState()
        val ignoredMatrixUser by produceState(
            initialValue = ignoredUserIds.map { MatrixUser(userId = it) },
            key1 = renderBlockedUsersDetail.value,
            key2 = ignoredUserIds
        ) {
            value = ignoredUserIds.map {
                if (renderBlockedUsersDetail.value) {
                    matrixClient.getProfile(it).getOrNull()
                } else {
                    null
                }
                    ?: MatrixUser(userId = it)
            }
        }

        fun handleEvents(event: BlockedUsersEvents) {
            when (event) {
                is BlockedUsersEvents.Unblock -> {
                    pendingUserToUnblock = event.userId
                    unblockUserAction.value = AsyncAction.Confirming
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
            blockedUsers = ignoredMatrixUser.toPersistentList(),
            unblockUserAction = unblockUserAction.value,
            eventSink = ::handleEvents
        )
    }

    private fun CoroutineScope.unblockUser(userId: UserId, asyncAction: MutableState<AsyncAction<Unit>>) = launch {
        runUpdatingState(asyncAction) {
            matrixClient.unignoreUser(userId)
        }
    }
}
