/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.invite.impl.response

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.features.invite.api.response.AcceptDeclineInviteState
import io.element.android.features.invite.api.response.AcceptDeclineInviteView
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.RoomId
import javax.inject.Inject

@ContributesBinding(SessionScope::class)
class AcceptDeclineInviteViewWrapper @Inject constructor() : AcceptDeclineInviteView {
    @Composable
    override fun Render(
        state: AcceptDeclineInviteState,
        onInviteAccepted: (RoomId) -> Unit,
        onInviteDeclined: (RoomId) -> Unit,
        modifier: Modifier,
    ) {
        AcceptDeclineInviteView(
            state = state,
            onInviteAccepted = onInviteAccepted,
            onInviteDeclined = onInviteDeclined,
            modifier = modifier
        )
    }
}
