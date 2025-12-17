/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.confirmation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.login.impl.di.QrCodeLoginScope
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.inputs

@ContributesNode(QrCodeLoginScope::class)
@AssistedInject
class QrCodeConfirmationNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
) : Node(buildContext = buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onCancel()
    }

    private val callback: Callback = callback()
    private val step = inputs<QrCodeConfirmationStep>()

    @Composable
    override fun View(modifier: Modifier) {
        QrCodeConfirmationView(
            step = step,
            onCancel = callback::onCancel,
        )
    }
}
