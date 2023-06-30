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

package io.element.android.features.preferences.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import io.element.android.features.logout.api.LogoutPreferencePresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.api.user.CurrentUserProvider
import io.element.android.libraries.matrix.api.user.MatrixUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class PreferencesRootPresenter @Inject constructor(
    private val logoutPresenter: LogoutPreferencePresenter,
    private val currentUserProvider: CurrentUserProvider,
    private val buildType: BuildType,
) : Presenter<PreferencesRootState> {

    @Composable
    override fun present(): PreferencesRootState {
        val matrixUser: MutableState<MatrixUser?> = rememberSaveable {
            mutableStateOf(null)
        }
        LaunchedEffect(Unit) {
            initialLoad(matrixUser)
        }

        val logoutState = logoutPresenter.present()
        val showDeveloperSettings = buildType != BuildType.RELEASE
        return PreferencesRootState(
            logoutState = logoutState,
            myUser = matrixUser.value,
            showDeveloperSettings = showDeveloperSettings
        )
    }

    private fun CoroutineScope.initialLoad(matrixUser: MutableState<MatrixUser?>) = launch {
        matrixUser.value = currentUserProvider.provide()
    }
}
