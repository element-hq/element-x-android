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

package io.element.android.features.login.impl.changeaccountprovider.form

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.Async

open class ChangeAccountProviderStateFormProvider : PreviewParameterProvider<ChangeAccountProviderFormState> {
    override val values: Sequence<ChangeAccountProviderFormState>
        get() = sequenceOf(
            aChangeAccountProviderFormState(),
            aChangeAccountProviderFormState(userInputResult = Async.Success(aHomeserverDataList())),
            // Add other state here
        )
}

fun aChangeAccountProviderFormState(
    userInput: String = "",
    userInputResult: Async<List<HomeserverData>> = Async.Uninitialized,
) = ChangeAccountProviderFormState(
    userInput = userInput,
    userInputResult = userInputResult,
    eventSink = {}
)

fun aHomeserverDataList(): List<HomeserverData> {
    return listOf(
        HomeserverData(
            userInput = "matrix",
            homeserverUrl = "https://matrix.org",
            isWellknownValid = true,
        ),
        HomeserverData(
            userInput = "matrix",
            homeserverUrl = "https://matrix.io",
            isWellknownValid = false,
        )
    )
}

fun aHomeserverData(): HomeserverData {
    return HomeserverData(
        userInput = "matrix",
        homeserverUrl = "https://matrix.org",
        isWellknownValid = true,
    )
}
