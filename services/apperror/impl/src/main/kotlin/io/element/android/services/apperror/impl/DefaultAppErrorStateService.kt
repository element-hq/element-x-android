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

package io.element.android.services.apperror.impl

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import io.element.android.services.apperror.api.AppErrorState
import io.element.android.services.apperror.api.AppErrorStateService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class DefaultAppErrorStateService @Inject constructor() : AppErrorStateService {
    private val currentAppErrorState = MutableStateFlow<AppErrorState>(AppErrorState.NoError)
    override val appErrorStateFlow: StateFlow<AppErrorState> = currentAppErrorState

    override fun showError(title: String, body: String) {
        currentAppErrorState.value = AppErrorState.Error(
            title = title,
            body = body,
            dismiss = {
                currentAppErrorState.value = AppErrorState.NoError
            },
        )
    }
}
