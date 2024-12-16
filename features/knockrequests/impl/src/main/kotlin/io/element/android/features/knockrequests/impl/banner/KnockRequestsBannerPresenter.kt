/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import io.element.android.features.knockrequests.impl.data.KnockRequestPresentable
import io.element.android.features.knockrequests.impl.data.KnockRequestsService
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.coroutine.mapState
import io.element.android.libraries.core.extensions.firstIfSingle
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.ui.room.canHandleKnockRequestsAsState
import io.element.android.libraries.matrix.ui.room.canInviteAsState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val ACCEPT_ERROR_DISPLAY_DURATION = 1500L

class KnockRequestsBannerPresenter @Inject constructor(
    private val room: MatrixRoom,
    private val knockRequestsService: KnockRequestsService,
    private val appCoroutineScope: CoroutineScope,
    private val featureFlagService: FeatureFlagService,
) : Presenter<KnockRequestsBannerState> {
    @Composable
    override fun present(): KnockRequestsBannerState {
        val knockRequests by remember {
            knockRequestsService.knockRequestsFlow.mapState { knockRequests ->
                knockRequests.dataOrNull().orEmpty()
                    .filter { !it.isSeen }
                    .toImmutableList()
            }
        }.collectAsState()

        val syncUpdateFlow = room.syncUpdateFlow.collectAsState()
        val canAccept by room.canInviteAsState(syncUpdateFlow.value)
        val canHandleKnockRequests by room.canHandleKnockRequestsAsState(syncUpdateFlow.value)
        val showAcceptError = remember { mutableStateOf(false) }
        val isKnockRequestsEnabled by featureFlagService.isFeatureEnabledFlow(FeatureFlags.Knock).collectAsState(false)

        val shouldShowBanner by remember {
            derivedStateOf {
                isKnockRequestsEnabled && canHandleKnockRequests && knockRequests.isNotEmpty()
            }
        }

        fun handleEvents(event: KnockRequestsBannerEvents) {
            when (event) {
                is KnockRequestsBannerEvents.AcceptSingleRequest -> {
                    appCoroutineScope.acceptSingleKnockRequest(
                        knockRequests = knockRequests,
                        displayAcceptError = showAcceptError,
                    )
                }
                is KnockRequestsBannerEvents.Dismiss -> {
                    appCoroutineScope.launch {
                        knockRequestsService.markAllKnockRequestsAsSeen()
                    }
                }
            }
        }

        return KnockRequestsBannerState(
            knockRequests = knockRequests,
            displayAcceptError = showAcceptError.value,
            canAccept = canAccept,
            isVisible = shouldShowBanner,
            eventSink = ::handleEvents,
        )
    }

    private fun CoroutineScope.acceptSingleKnockRequest(
        knockRequests: List<KnockRequestPresentable>,
        displayAcceptError: MutableState<Boolean>,
    ) = launch {
        val knockRequest = knockRequests.firstIfSingle()
        if (knockRequest != null) {
            knockRequestsService.acceptKnockRequest(knockRequest, optimistic = true)
                .onFailure {
                    displayAcceptError.value = true
                    delay(ACCEPT_ERROR_DISPLAY_DURATION)
                    displayAcceptError.value = false
                }
        }
    }
}
