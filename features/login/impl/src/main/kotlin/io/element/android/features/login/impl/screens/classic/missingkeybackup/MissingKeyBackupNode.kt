/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.classic.missingkeybackup

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.login.impl.BuildConfig
import io.element.android.libraries.architecture.callback
import timber.log.Timber

@ContributesNode(AppScope::class)
@AssistedInject
class MissingKeyBackupNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: MissingKeyBackupPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun navigateBack()
    }

    private val callback: Callback = callback()

    /**
     * Open Element Classic application.
     */
    private fun openClassic(context: Context) {
        context.packageManager.getLaunchIntentForPackage(
            BuildConfig.elementClassicPackage,
        )?.let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                // Should not happen, Element Classic must be installed for this screen to be displayed.
                Timber.e(e, "Element Classic app not found, cannot open it.")
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        val context = LocalContext.current
        MissingKeyBackupView(
            state = state,
            onBackClick = callback::navigateBack,
            onOpenClassicClick = {
                openClassic(context)
            },
            modifier = modifier,
        )
    }
}
