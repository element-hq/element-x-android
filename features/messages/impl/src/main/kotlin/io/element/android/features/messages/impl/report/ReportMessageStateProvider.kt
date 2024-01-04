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

package io.element.android.features.messages.impl.report

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData

open class ReportMessageStateProvider : PreviewParameterProvider<ReportMessageState> {
    override val values: Sequence<ReportMessageState>
        get() = sequenceOf(
            aReportMessageState(),
            aReportMessageState(reason = "This user is making the chat very toxic."),
            aReportMessageState(reason = "This user is making the chat very toxic.", blockUser = true),
            aReportMessageState(reason = "This user is making the chat very toxic.", blockUser = true, result = AsyncData.Loading()),
            aReportMessageState(reason = "This user is making the chat very toxic.", blockUser = true, result = AsyncData.Failure(Throwable("error"))),
            aReportMessageState(reason = "This user is making the chat very toxic.", blockUser = true, result = AsyncData.Success(Unit)),
            // Add other states here
        )
}

fun aReportMessageState(
    reason: String = "",
    blockUser: Boolean = false,
    result: AsyncData<Unit> = AsyncData.Uninitialized,
) = ReportMessageState(
    reason = reason,
    blockUser = blockUser,
    result = result,
    eventSink = {}
)
