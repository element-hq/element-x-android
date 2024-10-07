/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.setup

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.di.SessionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ContributesNode(SessionScope::class)
class SecureBackupSetupNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SecureBackupSetupPresenter.Factory,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val isChangeRecoveryKeyUserStory: Boolean,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()

    private val presenter = presenterFactory.create(inputs.isChangeRecoveryKeyUserStory)

    @Composable
    override fun View(modifier: Modifier) {
        val coroutineScope = rememberCoroutineScope()
        val state = presenter.present()
        SecureBackupSetupView(
            state = state,
            onSuccess = {
                coroutineScope.postSuccessSnackbar()
                navigateUp()
            },
            onBackClick = ::navigateUp,
            modifier = modifier,
        )
    }

    private fun CoroutineScope.postSuccessSnackbar() = launch {
        snackbarDispatcher.post(
            SnackbarMessage(
                messageResId = if (inputs.isChangeRecoveryKeyUserStory) {
                    R.string.screen_recovery_key_change_success
                } else {
                    R.string.screen_recovery_key_setup_success
                }
            )
        )
    }
}
