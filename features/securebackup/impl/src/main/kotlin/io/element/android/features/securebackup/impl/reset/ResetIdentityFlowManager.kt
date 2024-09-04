/*
 * Copyright (c) 2024 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.securebackup.impl.reset

import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.encryption.IdentityResetHandle
import io.element.android.libraries.matrix.api.verification.SessionVerificationService
import io.element.android.libraries.matrix.api.verification.SessionVerifiedStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

class ResetIdentityFlowManager @Inject constructor(
    private val matrixClient: MatrixClient,
    @SessionCoroutineScope private val sessionCoroutineScope: CoroutineScope,
    private val sessionVerificationService: SessionVerificationService,
) {
    private val resetHandleFlow: MutableStateFlow<AsyncData<IdentityResetHandle?>> = MutableStateFlow(AsyncData.Uninitialized)
    val currentHandleFlow: StateFlow<AsyncData<IdentityResetHandle?>> = resetHandleFlow
    private var whenResetIsDoneWaitingJob: Job? = null

    fun whenResetIsDone(block: () -> Unit) {
        whenResetIsDoneWaitingJob = sessionCoroutineScope.launch {
            sessionVerificationService.sessionVerifiedStatus.filterIsInstance<SessionVerifiedStatus.Verified>().first()
            block()
        }
    }

    fun getResetHandle(): StateFlow<AsyncData<IdentityResetHandle?>> {
        return if (resetHandleFlow.value.isLoading() || resetHandleFlow.value.isSuccess()) {
            resetHandleFlow
        } else {
            resetHandleFlow.value = AsyncData.Loading()

            sessionCoroutineScope.launch {
                matrixClient.encryptionService().startIdentityReset()
                    .onSuccess { handle ->
                        resetHandleFlow.value = AsyncData.Success(handle)
                    }
                    .onFailure {
                        resetHandleFlow.value = AsyncData.Failure(it)
                    }
            }

            resetHandleFlow
        }
    }

    suspend fun cancel() {
        currentHandleFlow.value.dataOrNull()?.cancel()
        resetHandleFlow.value = AsyncData.Uninitialized

        whenResetIsDoneWaitingJob?.cancel()
        whenResetIsDoneWaitingJob = null
    }
}
