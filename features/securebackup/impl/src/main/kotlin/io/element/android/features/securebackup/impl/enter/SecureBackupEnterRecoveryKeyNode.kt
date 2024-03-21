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

package io.element.android.features.securebackup.impl.enter

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.securebackup.impl.R
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.di.SessionScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@ContributesNode(SessionScope::class)
class SecureBackupEnterRecoveryKeyNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: SecureBackupEnterRecoveryKeyPresenter,
    private val snackbarDispatcher: SnackbarDispatcher,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onEnterRecoveryKeySuccess()
    }

    private val callback = plugins<Callback>().first()

    @Composable
    override fun View(modifier: Modifier) {
        val coroutineScope = rememberCoroutineScope()
        val state = presenter.present()
        SecureBackupEnterRecoveryKeyView(
            state = state,
            modifier = modifier,
            onDone = {
                coroutineScope.postSuccessSnackbar()
                callback.onEnterRecoveryKeySuccess()
            },
            onBackClicked = ::navigateUp,
        )
    }

    private fun CoroutineScope.postSuccessSnackbar() = launch {
        snackbarDispatcher.post(
            SnackbarMessage(
                messageResId = R.string.screen_recovery_key_confirm_success
            )
        )
    }
}
