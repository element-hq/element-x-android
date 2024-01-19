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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.impl.util.openLearnMorePage
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.auth.OidcDetails

@ContributesNode(AppScope::class)
class ConfirmAccountProviderNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: ConfirmAccountProviderPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val isAccountCreation: Boolean,
    ) : NodeInputs

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(
        ConfirmAccountProviderPresenter.Params(
            isAccountCreation = inputs.isAccountCreation,
        )
    )

    interface Callback : Plugin {
        fun onLoginPasswordNeeded()
        fun onOidcDetails(oidcDetails: OidcDetails)
        fun onChangeAccountProvider()
    }

    private fun onOidcDetails(data: OidcDetails) {
        plugins<Callback>().forEach { it.onOidcDetails(data) }
    }

    private fun onLoginPasswordNeeded() {
        plugins<Callback>().forEach { it.onLoginPasswordNeeded() }
    }

    private fun onChangeAccountProvider() {
        plugins<Callback>().forEach { it.onChangeAccountProvider() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        ConfirmAccountProviderView(
            state = state,
            modifier = modifier,
            onOidcDetails = ::onOidcDetails,
            onLoginPasswordNeeded = ::onLoginPasswordNeeded,
            onChange = ::onChangeAccountProvider,
            onLearnMoreClicked = { openLearnMorePage(context) },
        )
    }
}
