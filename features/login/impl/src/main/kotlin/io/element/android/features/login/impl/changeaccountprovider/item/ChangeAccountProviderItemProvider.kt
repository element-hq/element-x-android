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

package io.element.android.features.login.impl.changeaccountprovider.item

import androidx.compose.ui.tooling.preview.PreviewParameterProvider

open class ChangeAccountProviderItemProvider : PreviewParameterProvider<AccountProviderItem> {
    override val values: Sequence<AccountProviderItem>
        get() = sequenceOf(
            aChangeAccountProviderItem(),
            aChangeAccountProviderItem().copy(subtitle = null),
            aChangeAccountProviderItem().copy(title = "Other", subtitle = null, isPublic = false, isMatrixOrg = false),
            // Add other state here
        )
}

fun aChangeAccountProviderItem() = AccountProviderItem(
    title = "matrix.org",
    subtitle = "Matrix.org is an open network for secure, decentralized communication.",
    isPublic = true,
    isMatrixOrg = true,
)
