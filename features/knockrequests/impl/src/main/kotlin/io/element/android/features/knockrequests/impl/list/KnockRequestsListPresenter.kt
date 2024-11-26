/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.runtime.Composable
import io.element.android.libraries.architecture.Presenter
import javax.inject.Inject

class KnockRequestsListPresenter @Inject constructor() : Presenter<KnockRequestsListState> {

    @Composable
    override fun present(): KnockRequestsListState {

        fun handleEvents(event: KnockRequestsListEvents) {
            when (event) {
                KnockRequestsListEvents.AcceptAll -> Unit
            }
        }

        return KnockRequestsListState(
            eventSink = ::handleEvents
        )
    }
}
