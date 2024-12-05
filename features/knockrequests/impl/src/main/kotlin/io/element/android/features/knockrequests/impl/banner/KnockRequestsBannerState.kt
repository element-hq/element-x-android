/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import io.element.android.features.knockrequests.impl.KnockRequest
import io.element.android.features.knockrequests.impl.getBestName
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.ui.strings.CommonPlurals
import io.element.android.libraries.ui.strings.CommonStrings
import kotlinx.collections.immutable.ImmutableList

@Immutable
sealed interface KnockRequestsBannerState {
    data object Hidden : KnockRequestsBannerState
    data class Visible(
        val knockRequests: ImmutableList<KnockRequest>,
        val acceptAction: AsyncAction<Unit>,
        val canAccept: Boolean,
    ) : KnockRequestsBannerState {

        val subtitle = if (knockRequests.size == 1) {
            knockRequests.first().userId.value
        } else {
            null
        }

        val reason = if (knockRequests.size == 1) {
            knockRequests.first().reason
        } else {
            null
        }

        @Composable
        fun formattedTitle(): String {
            return when (knockRequests.size) {
                0 -> ""
                1 -> stringResource(CommonStrings.screen_room_single_knock_request_title, knockRequests.first().getBestName())
                else -> {
                    val firstRequest = knockRequests.first()
                    val otherRequestsCount = knockRequests.size - 1
                    pluralStringResource(
                        id = CommonPlurals.screen_room_multiple_knock_requests_title,
                        count = otherRequestsCount,
                        firstRequest.getBestName(),
                        otherRequestsCount
                    )
                }
            }
        }
    }
}
