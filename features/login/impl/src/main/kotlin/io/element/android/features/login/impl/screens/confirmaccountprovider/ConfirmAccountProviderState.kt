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

package io.element.android.features.login.impl.screens.confirmaccountprovider

import io.element.android.features.login.impl.accountprovider.AccountProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.auth.OidcDetails

// Do not use default value, so no member get forgotten in the presenters.
data class ConfirmAccountProviderState(
    val accountProvider: AccountProvider,
    val isAccountCreation: Boolean,
    val loginFlow: AsyncData<LoginFlow>,
    val eventSink: (ConfirmAccountProviderEvents) -> Unit
) {
    val submitEnabled: Boolean get() = accountProvider.url.isNotEmpty() && (loginFlow is AsyncData.Uninitialized || loginFlow is AsyncData.Loading)
}

sealed interface LoginFlow {
    data object PasswordLogin : LoginFlow
    data class OidcFlow(val oidcDetails: OidcDetails) : LoginFlow
}
