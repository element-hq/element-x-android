/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import io.element.android.features.knockrequests.impl.R
import io.element.android.features.knockrequests.impl.data.KnockRequestPresentable
import kotlinx.collections.immutable.ImmutableList

data class KnockRequestsBannerState(
    val isVisible: Boolean,
    val knockRequests: ImmutableList<KnockRequestPresentable>,
    val displayAcceptError: Boolean,
    val canAccept: Boolean,
    val eventSink: (KnockRequestsBannerEvents) -> Unit,
) {
    val subtitle = knockRequests.singleOrNull()?.userId?.value
    val reason = knockRequests.singleOrNull()?.reason

    @Composable
    fun formattedTitle(): String {
        return when (knockRequests.size) {
            0 -> ""
            1 -> stringResource(R.string.screen_room_single_knock_request_title, knockRequests.first().getBestName())
            else -> {
                val firstRequest = knockRequests.first()
                val otherRequestsCount = knockRequests.size - 1
                pluralStringResource(
                    id = R.plurals.screen_room_multiple_knock_requests_title,
                    count = otherRequestsCount,
                    firstRequest.getBestName(),
                    otherRequestsCount
                )
            }
        }
    }
}
