/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.waitlistscreen

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.impl.screens.loginpassword.LoginFormState
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.AppScope

@ContributesNode(AppScope::class)
class WaitListNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: WaitListPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(val loginFormState: LoginFormState) : NodeInputs

    private val inputs: Inputs = inputs()
    private val presenter = presenterFactory.create(inputs.loginFormState)

    interface Callback : Plugin {
        fun onCancelClick()
    }

    private fun onCancelClick() {
        plugins<Callback>().forEach { it.onCancelClick() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        WaitListView(
            state = state,
            onCancelClick = ::onCancelClick,
            modifier = modifier
        )
    }
}
