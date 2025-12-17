/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.scan

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
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData

@ContributesNode(QrCodeLoginScope::class)
@AssistedInject
class QrCodeScanNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: QrCodeScanPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun handleScannedCode(qrCodeLoginData: MatrixQrCodeLoginData)
        fun cancel()
    }

    private val callback: Callback = callback()

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        QrCodeScanView(
            state = state,
            onQrCodeDataReady = callback::handleScannedCode,
            onBackClick = callback::cancel,
            modifier = modifier
        )
    }
}
