/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.knockrequests.impl.KnockRequest
import io.element.android.features.knockrequests.impl.aKnockRequest
import io.element.android.libraries.architecture.AsyncAction
import kotlinx.collections.immutable.toImmutableList

class KnockRequestsBannerStateProvider : PreviewParameterProvider<KnockRequestsBannerState> {
    override val values: Sequence<KnockRequestsBannerState>
        get() = sequenceOf(
            KnockRequestsBannerState.Hidden,
            aVisibleKnockRequestsBannerState(),
            aVisibleKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequest(),
                    aKnockRequest(displayName = "Alice")
                )
            ),
            aVisibleKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequest(),
                    aKnockRequest(displayName = "Alice"),
                    aKnockRequest(displayName = "Bob"),
                    aKnockRequest(displayName = "Charlie")
                )
            ),
            aVisibleKnockRequestsBannerState(
                canAccept = false
            ),
            aVisibleKnockRequestsBannerState(
                acceptAction = AsyncAction.Loading
            ),
            aVisibleKnockRequestsBannerState(
                acceptAction = AsyncAction.Failure(Throwable())
            ),
        )
}

fun aVisibleKnockRequestsBannerState(
    knockRequests: List<KnockRequest> = listOf(aKnockRequest()),
    acceptAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    canAccept: Boolean = true,
) = KnockRequestsBannerState.Visible(
    knockRequests = knockRequests.toImmutableList(),
    acceptAction = acceptAction,
    canAccept = canAccept
)
