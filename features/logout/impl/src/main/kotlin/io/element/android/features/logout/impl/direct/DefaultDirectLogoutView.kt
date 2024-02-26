/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            onConfirmClicked = {
                eventSink(DirectLogoutEvents.Logout(ignoreSdkError = false))
            },
            onForceLogoutClicked = {
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
