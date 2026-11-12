/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securebackup.impl.generate

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.securebackup.api.SecureBackupRecoveryKeyEntryPoint
import io.element.android.libraries.architecture.NodeInputs
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.di.SessionScope

@ContributesNode(SessionScope::class)
@AssistedInject
class SecureBackupGenerateKeyNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: SecureBackupGenerateKeyPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    data class Inputs(
        val isReplacingExistingKey: Boolean,
        val doneButtonText: Int,
    ) : NodeInputs

    private val inputs = inputs<Inputs>()
    private val callback: SecureBackupRecoveryKeyEntryPoint.Callback = callback()
    private val presenter = presenterFactory.create(inputs.isReplacingExistingKey)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        SecureBackupGenerateKeyView(
            state = state,
            doneButtonText = inputs.doneButtonText,
            onDone = callback::onDone,
            onBackClick = ::navigateUp,
            modifier = modifier,
        )
    }
}
