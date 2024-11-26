/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject

class KnockRequestsListPresenter @Inject constructor() : Presenter<KnockRequestsListState> {

    @Composable
    override fun present(): KnockRequestsListState {
        val actions = remember {
            mutableStateOf<KnockRequestsCurrentAction>(KnockRequestsCurrentAction.None)
        }

        fun handleEvents(event: KnockRequestsListEvents) {
            when (event) {
                KnockRequestsListEvents.AcceptAll -> Unit
                is KnockRequestsListEvents.Accept -> Unit
                is KnockRequestsListEvents.Decline -> Unit
                KnockRequestsListEvents.DismissCurrentAction -> {
                    actions.value = KnockRequestsCurrentAction.None
                }
            }
        }

        return KnockRequestsListState(
            knockRequests = AsyncData.Success(persistentListOf()),
            currentAction = actions.value,
            eventSink = ::handleEvents
        )
    }
}
