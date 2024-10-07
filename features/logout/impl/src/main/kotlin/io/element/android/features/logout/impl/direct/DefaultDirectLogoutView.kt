/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.logout.impl.direct

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.logout.api.direct.DirectLogoutEvents
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.api.direct.DirectLogoutStateProvider
import io.element.android.features.logout.api.direct.DirectLogoutView
import io.element.android.features.logout.impl.ui.LogoutActionDialog
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.di.SessionScope
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class DefaultDirectLogoutView @Inject constructor() : DirectLogoutView {
    @Composable
    override fun Render(
        state: DirectLogoutState,
        onSuccessLogout: (logoutUrlResult: String?) -> Unit,
    ) {
        val eventSink = state.eventSink
        LogoutActionDialog(
            state.logoutAction,
            onConfirmClick = {
                eventSink(DirectLogoutEvents.Logout(ignoreSdkError = false))
            },
            onForceLogoutClick = {
                eventSink(DirectLogoutEvents.Logout(ignoreSdkError = true))
            },
            onDismissDialog = {
                eventSink(DirectLogoutEvents.CloseDialogs)
            },
            onSuccessLogout = {
                onSuccessLogout(it)
            },
        )
    }
}

@PreviewsDayNight
@Composable
internal fun DefaultDirectLogoutViewPreview(
    @PreviewParameter(DirectLogoutStateProvider::class) state: DirectLogoutState,
) = ElementPreview {
    DefaultDirectLogoutView().Render(
        state = state,
        onSuccessLogout = {},
    )
}
