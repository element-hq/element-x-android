/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.confirmation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.login.impl.di.QrCodeLoginScope
import io.element.android.libraries.architecture.inputs

@ContributesNode(QrCodeLoginScope::class)
class QrCodeConfirmationNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : Node(buildContext = buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onCancel()
    }

    private val step = inputs<QrCodeConfirmationStep>()

    private fun onCancel() {
        plugins<Callback>().forEach { it.onCancel() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        QrCodeConfirmationView(
            step = step,
            onCancel = ::onCancel,
        )
    }
}
