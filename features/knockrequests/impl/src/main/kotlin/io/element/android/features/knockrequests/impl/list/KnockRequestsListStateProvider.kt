/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.knockrequests.impl.list

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.knockrequests.impl.data.KnockRequestPermissions
import io.element.android.features.knockrequests.impl.data.KnockRequestPresentable
import io.element.android.features.knockrequests.impl.data.aKnockRequestPresentable
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
                        aKnockRequestPresentable()
                    )
                ),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequestPresentable(
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
                        aKnockRequestPresentable(),
                        aKnockRequestPresentable(
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
                        aKnockRequestPresentable()
                    )
                ),
                currentAction = KnockRequestsAction.AcceptAll,
                asyncAction = AsyncAction.ConfirmingNoParams,
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequestPresentable()
                    )
                ),
                currentAction = KnockRequestsAction.AcceptAll,
                asyncAction = AsyncAction.Loading,
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequestPresentable()
                    )
                ),
                permissions = KnockRequestPermissions(
                    canAccept = false,
                    canDecline = true,
                    canBan = true,
                ),
                currentAction = KnockRequestsAction.AcceptAll,
                asyncAction = AsyncAction.Failure(RuntimeException("Failed to accept all")),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequestPresentable()
                    )
                ),
                permissions = KnockRequestPermissions(
                    canAccept = true,
                    canDecline = false,
                    canBan = true,
                ),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequestPresentable()
                    )
                ),
                permissions = KnockRequestPermissions(
                    canAccept = false,
                    canDecline = false,
                    canBan = true,
                ),
            ),
            aKnockRequestsListState(
                knockRequests = AsyncData.Success(
                    persistentListOf(
                        aKnockRequestPresentable()
                    )
                ),
                permissions = KnockRequestPermissions(
                    canAccept = true,
                    canDecline = true,
                    canBan = false,
                ),
            ),
        )
}

fun aKnockRequestsListState(
    knockRequests: AsyncData<ImmutableList<KnockRequestPresentable>> = AsyncData.Success(persistentListOf()),
    currentAction: KnockRequestsAction = KnockRequestsAction.None,
    asyncAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    permissions: KnockRequestPermissions = KnockRequestPermissions(
        canAccept = true,
        canDecline = true,
        canBan = true,
    ),
    eventSink: (KnockRequestsListEvents) -> Unit = {},
) = KnockRequestsListState(
    knockRequests = knockRequests,
    currentAction = currentAction,
    asyncAction = asyncAction,
    permissions = permissions,
    eventSink = eventSink,
)
