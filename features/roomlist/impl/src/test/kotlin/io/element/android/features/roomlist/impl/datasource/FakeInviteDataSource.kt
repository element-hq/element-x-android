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

package io.element.android.features.roomlist.impl.datasource

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import io.element.android.features.roomlist.impl.InvitesState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeInviteDataSource(
    private val flow: Flow<InvitesState> = flowOf()
) : InviteStateDataSource {
    @Composable
    override fun inviteState(): InvitesState {
        val state = flow.collectAsState(initial = InvitesState.NoInvites)
        return state.value
    }
}
