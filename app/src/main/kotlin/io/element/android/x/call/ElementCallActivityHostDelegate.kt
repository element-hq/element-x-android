/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.call

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import io.element.android.call.api.ElementCallController
import io.element.android.call.ui.ElementCallActivity
import io.element.android.call.ui.theme.ElementCallStyle
import io.element.android.features.callnative.api.NativeCallScreenHost

/**
 * The native call screen's host, which `ElementXApplication` implements by delegating here: the library looks
 * its host up on the `Application`, the only place it can find one when the screen opens over the lock screen.
 *
 * The Application has to choose its delegate before it has a dependency graph, so the [NativeCallScreenHost]
 * binding is handed over later, in [bind], from `Application.onCreate`, which always runs before the call
 * screen's own `onCreate`.
 */
class ElementCallActivityHostDelegate : ElementCallActivity.Host {
    private lateinit var provider: () -> NativeCallScreenHost
    private val host: NativeCallScreenHost by lazy { provider() }

    fun bind(provider: () -> NativeCallScreenHost) {
        this.provider = provider
    }

    override val elementCallController: ElementCallController?
        get() = host.elementCallController

    override fun leaveCallScreenIntent(context: Context): Intent = host.leaveCallScreenIntent(context)

    override fun onCallActivityCreated(activity: ComponentActivity, controller: ElementCallController) =
        host.onCallActivityCreated(activity, controller)

    @Composable
    override fun CallScreenContent(content: @Composable (style: ElementCallStyle?) -> Unit) =
        host.CallScreenContent(content)
}
