/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.banner

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.knockrequests.impl.data.KnockRequestPresentable
import io.element.android.features.knockrequests.impl.data.aKnockRequestPresentable
import kotlinx.collections.immutable.toImmutableList

class KnockRequestsBannerStateProvider : PreviewParameterProvider<KnockRequestsBannerState> {
    override val values: Sequence<KnockRequestsBannerState>
        get() = sequenceOf(
            aKnockRequestsBannerState(),
            aKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequestPresentable(
                        reason = "A very long reason that should probably be truncated, " +
                            "but could be also expanded so you can see it over the lines, wow," +
                            "very amazing reason, I know, right, I'm so good at writing reasons."
                    )
                )
            ),
            aKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequestPresentable(),
                    aKnockRequestPresentable(displayName = "Alice")
                )
            ),
            aKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequestPresentable(),
                    aKnockRequestPresentable(displayName = "Alice"),
                    aKnockRequestPresentable(displayName = "Bob"),
                    aKnockRequestPresentable(displayName = "Charlie")
                )
            ),
            aKnockRequestsBannerState(
                canAccept = false
            ),
            aKnockRequestsBannerState(
                displayAcceptError = true
            ),
            aKnockRequestsBannerState(
                knockRequests = listOf(
                    aKnockRequestPresentable(
                        displayName = "A_very_long_display_name_so_that_the_text_can_be_displayed_on_multiple_lines"
                    )
                )
            ),
        )
}

fun aKnockRequestsBannerState(
    knockRequests: List<KnockRequestPresentable> = listOf(aKnockRequestPresentable()),
    displayAcceptError: Boolean = false,
    canAccept: Boolean = true,
    isVisible: Boolean = true,
    eventSink: (KnockRequestsBannerEvents) -> Unit = {}
) = KnockRequestsBannerState(
    knockRequests = knockRequests.toImmutableList(),
    displayAcceptError = displayAcceptError,
    canAccept = canAccept,
    isVisible = isVisible,
    eventSink = eventSink,
)
