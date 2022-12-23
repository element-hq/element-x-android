/*
 * Copyright (c) 2022 New Vector Ltd
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

package io.element.android.x.features.preferences.user

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.mvrx.compose.collectAsState
import com.airbnb.mvrx.compose.mavericksViewModel
import io.element.android.x.matrix.ui.components.MatrixUserHeader
import io.element.android.x.matrix.ui.viewmodels.user.UserViewModel
import io.element.android.x.matrix.ui.viewmodels.user.UserViewState

@Composable
fun UserPreferences(
    modifier: Modifier = Modifier,
    viewModel: UserViewModel = mavericksViewModel(),
) {
    val user by viewModel.collectAsState(UserViewState::user)
    when (user()) {
        null -> Spacer(modifier = modifier.height(1.dp))
        else -> MatrixUserHeader(
            modifier = modifier,
            matrixUser = user.invoke()!!
        )
    }
}
