/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.error

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
import io.element.android.features.login.impl.qrcode.QrCodeErrorScreenType
import io.element.android.libraries.architecture.inputs
import io.element.android.libraries.core.meta.BuildMeta

@ContributesNode(QrCodeLoginScope::class)
class QrCodeErrorNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val buildMeta: BuildMeta,
) : Node(buildContext = buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onRetry()
    }

    private fun onRetry() {
        plugins<Callback>().forEach { it.onRetry() }
    }

    private val qrCodeErrorScreenType = inputs<QrCodeErrorScreenType>()

    @Composable
    override fun View(modifier: Modifier) {
        QrCodeErrorView(
            modifier = modifier,
            errorScreenType = qrCodeErrorScreenType,
            appName = buildMeta.productionApplicationName,
            onRetry = ::onRetry,
        )
    }
}
