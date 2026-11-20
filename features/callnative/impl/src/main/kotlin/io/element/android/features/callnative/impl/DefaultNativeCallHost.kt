/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.impl

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.zacsweers.metro.ContributesBinding
import io.element.android.call.ui.ElementCallOverlay
import io.element.android.features.callnative.api.NativeCallHost
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.MatrixClient

/**
 * Draws the session's native call around the logged-in content.
 *
 * Session-scoped, unlike the entry point: this renders inside the logged-in navigation, so the
 * session is already in scope and there is no need to go looking for one. The stack it draws is not,
 * so this asks the app-scoped registry for its own session's controller.
 *
 * That controller only exists once a call has been placed or answered. Until then the overlay draws
 * the content alone.
 */
@ContributesBinding(SessionScope::class)
class DefaultNativeCallHost(
    private val client: MatrixClient,
    private val controllers: ElementCallControllers,
) : NativeCallHost {
    @Composable
    override fun Render(modifier: Modifier, content: @Composable (Modifier) -> Unit) {
        val controller by remember(client.sessionId) { controllers.controller(client.sessionId) }
            .collectAsState(initial = null)
        // Composed even with no controller yet, so the content never moves in the tree and keeps its state.
        // Only the minimized bar is drawn here: the full-screen call is ElementCallActivity, see NativeCallScreenHost.
        ElementCallOverlay(
            controller = controller,
            modifier = modifier,
            style = rememberElementXCallStyle(),
            content = content,
        )
    }
}
