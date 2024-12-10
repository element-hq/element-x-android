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
            aKnockRequestsBannerState(),
            aKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequest(
                        reason = "A very long reason that should probably be truncated, " +
                            "but could be also expanded so you can see it over the lines, wow," +
                            "very amazing reason, I know, right, I'm so good at writing reasons."
                    )
                )
            ),
            aKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequest(),
                    aKnockRequest(displayName = "Alice")
                )
            ),
            aKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequest(),
                    aKnockRequest(displayName = "Alice"),
                    aKnockRequest(displayName = "Bob"),
                    aKnockRequest(displayName = "Charlie")
                )
            ),
            aKnockRequestsBannerState(
                canAccept = false
            ),
            aKnockRequestsBannerState(
                acceptAction = AsyncAction.Loading
            ),
            aKnockRequestsBannerState(
                acceptAction = AsyncAction.Failure(Throwable("Failed to accept knock"))
            ),
            aKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequest(
                        displayName = "A_very_long_display_name_so_that_the_text_can_be_displayed_on_multiple_lines"
                    )
                )
            ),
        )
}

fun aKnockRequestsBannerState(
    knockRequests: List<KnockRequest> = listOf(aKnockRequest()),
    acceptAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    canAccept: Boolean = true,
    isVisible: Boolean = true,
    eventSink: (KnockRequestsBannerEvents) -> Unit = {}
) = KnockRequestsBannerState(
    knockRequests = knockRequests.toImmutableList(),
    acceptAction = acceptAction,
    canAccept = canAccept,
    isVisible = isVisible,
    eventSink = eventSink,
)
