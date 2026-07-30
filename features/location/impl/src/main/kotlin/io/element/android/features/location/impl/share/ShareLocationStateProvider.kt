/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.share

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.location.impl.common.ui.LocationConstraintsDialogState
import io.element.android.features.location.impl.common.userlocation.UserLocationState
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes

private const val APP_NAME = "ApplicationName"

class ShareLocationStateProvider : PreviewParameterProvider<ShareLocationState> {
    override val values: Sequence<ShareLocationState>
        get() = sequenceOf(
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.None,
                trackUserPosition = false,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionDenied),
                trackUserPosition = false,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.PermissionRationale),
                trackUserPosition = false,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.Constraints(LocationConstraintsDialogState.LocationServiceDisabled),
                trackUserPosition = false,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.None,
                trackUserPosition = false,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.None,
                trackUserPosition = true,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.None,
                trackUserPosition = true,
                canShareLiveLocation = true,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.LiveLocationDisclaimer,
                trackUserPosition = true,
                canShareLiveLocation = true,
            ),
            aShareLocationState(
                dialogState = ShareLocationState.Dialog.LiveLocationDurations(
                    persistentListOf(
                        LiveLocationDuration(15.minutes, "15 minutes"),
                        LiveLocationDuration(1.hours, "1 hour"),
                        LiveLocationDuration(8.hours, "8 hours"),
                    )
                ),
                trackUserPosition = true,
                canShareLiveLocation = true,
            ),
            aShareLocationState(
                customMapStyleUrl = AsyncData.Loading(),
            ),
        )
}

fun aShareLocationState(
    customMapStyleUrl: AsyncData<String?> = AsyncData.Success(null),
    currentUser: MatrixUser = MatrixUser(UserId("@user:matrix.org")),
    dialogState: ShareLocationState.Dialog = ShareLocationState.Dialog.None,
    trackUserPosition: Boolean = false,
    userLocationState: UserLocationState = UserLocationState(null),
    canShareLiveLocation: Boolean = false,
    appName: String = APP_NAME,
    startLiveLocationAction: AsyncAction<Unit> = AsyncAction.Uninitialized,
    eventSink: (ShareLocationEvent) -> Unit = {},
): ShareLocationState {
    return ShareLocationState(
        customMapStyleUrl = customMapStyleUrl,
        currentUser = currentUser,
        dialogState = dialogState,
        trackUserLocation = trackUserPosition,
        userLocationState = userLocationState,
        canShareLiveLocation = canShareLiveLocation,
        appName = appName,
        startLiveLocationAction = startLiveLocationAction,
        eventSink = eventSink
    )
}
