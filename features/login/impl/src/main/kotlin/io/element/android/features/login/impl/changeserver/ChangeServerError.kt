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

package io.element.android.features.login.impl.changeserver

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.element.android.libraries.ui.strings.R
import org.matrix.rustcomponents.sdk.AuthenticationException

sealed class ChangeServerError : Throwable() {
    data class Footer(@StringRes val messageId: Int) : ChangeServerError() {
        @Composable
        fun message(): String = stringResource(messageId)
    }
    object SlidingSyncAlert : ChangeServerError()

    companion object {
        fun from(error: Throwable): ChangeServerError = when (error) {
            is AuthenticationException.SlidingSyncNotAvailable -> SlidingSyncAlert
            else -> Footer(R.string.server_selection_invalid_homeserver_error)
        }
    }
}
