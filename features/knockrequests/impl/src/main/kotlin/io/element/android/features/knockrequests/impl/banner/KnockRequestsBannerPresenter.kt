/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.Presenter
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject

class KnockRequestsBannerPresenter @Inject constructor() : Presenter<KnockRequestsBannerState> {
    @Composable
    override fun present(): KnockRequestsBannerState {
        var shouldShowBanner by remember { mutableStateOf(false) }

        fun handleEvents(event: KnockRequestsBannerEvents) {
            when (event) {
                is KnockRequestsBannerEvents.Accept -> Unit
                is KnockRequestsBannerEvents.Dismiss -> {
                    shouldShowBanner = false
                }
            }
        }

        return KnockRequestsBannerState(
            knockRequests = persistentListOf(),
            acceptAction = AsyncAction.Uninitialized,
            canAccept = false,
            isVisible = shouldShowBanner,
            eventSink = ::handleEvents,
        )
    }
}
