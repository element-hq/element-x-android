/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.qrcode.intro

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

@ContributesNode(QrCodeLoginScope::class)
class QrCodeIntroNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: QrCodeIntroPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun onCancelClicked()
        fun onContinue()
    }

    private fun onCancelClicked() {
        plugins<Callback>().forEach { it.onCancelClicked() }
    }

    private fun onContinue() {
        plugins<Callback>().forEach { it.onContinue() }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        QrCodeIntroView(
            state = state,
            onBackClick = ::onCancelClicked,
            onContinue = ::onContinue,
            modifier = modifier
        )
    }
}
