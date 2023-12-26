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
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.logout.api.direct.DirectLogoutEvents
import io.element.android.features.logout.api.direct.DirectLogoutState
import io.element.android.features.logout.api.direct.DirectLogoutView
import io.element.android.features.logout.impl.ui.LogoutActionDialog
import io.element.android.features.logout.impl.ui.LogoutConfirmationDialog
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
        // Log out confirmation dialog
        if (state.showConfirmationDialog) {
            LogoutConfirmationDialog(
                onSubmitClicked = {
                    eventSink(DirectLogoutEvents.Logout(ignoreSdkError = false))
                },
                onDismiss = {
                    eventSink(DirectLogoutEvents.CloseDialogs)
                }
            )
        }

        LogoutActionDialog(
            state.logoutAction,
            onForceLogoutClicked = {
                eventSink(DirectLogoutEvents.Logout(ignoreSdkError = true))
            },
            onDismissError = {
                eventSink(DirectLogoutEvents.CloseDialogs)
            },
            onSuccessLogout = {
                onSuccessLogout(it)
            },
        )
    }
}
