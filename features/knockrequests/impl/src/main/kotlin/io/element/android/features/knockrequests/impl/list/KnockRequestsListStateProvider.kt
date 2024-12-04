/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.knockrequests.impl.KnockRequest
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

open class KnockRequestsListStateProvider : PreviewParameterProvider<KnockRequestsListState> {
    override val values: Sequence<KnockRequestsListState>
        get() = sequenceOf(
            aKnockRequestsListState(
                knockRequests = AsyncData.Loading(),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf()
                ),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequest()
                    )
                ),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequest(
                            reason = "A very long reason that should probably be truncated, " +
                                "but could be also expanded so you can see it over the lines, wow," +
                                "very amazing reason, I know, right, I'm so good at writing reasons."
                        )
                    )
                ),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequest(),
                        aKnockRequest(
                            userId = UserId("@user:example.com"),
                            displayName = null,
                            avatarUrl = null,
                            reason = null,
                        )
                    )
                ),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequest()
                    )
                ),
                currentAction = KnockRequestsCurrentAction.AcceptAll(AsyncAction.Loading),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequest()
                    )
                ),
                canAccept = false,
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequest()
                    )
                ),
                canDecline = false,
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequest()
                    )
                ),
                canAccept = false,
                canDecline = false,
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequest()
                    )
                ),
                canBan = false,
            ),
        )
}

fun aKnockRequest(
    userId: UserId = UserId("@jacob_ross:example.com"),
    displayName: String? = "Jacob Ross",
    avatarUrl: String? = null,
    reason: String? = "Hi, I would like to get access to this room please.",
    formattedDate: String = "20 Nov 2024",
) = KnockRequest(
    userId = userId,
    displayName = displayName,
    avatarUrl = avatarUrl,
    reason = reason,
    formattedDate = formattedDate,
)

fun aKnockRequestsListState(
    knockRequests: AsyncData<ImmutableList<KnockRequest>> = AsyncData.Success(persistentListOf()),
    currentAction: KnockRequestsCurrentAction = KnockRequestsCurrentAction.None,
    canAccept: Boolean = true,
    canDecline: Boolean = true,
    canBan: Boolean = true,
    eventSink: (KnockRequestsListEvents) -> Unit = {},
) = KnockRequestsListState(
    knockRequests = knockRequests,
    currentAction = currentAction,
    canAccept = canAccept,
    canDecline = canDecline,
    canBan = canBan,
    eventSink = eventSink,
)
