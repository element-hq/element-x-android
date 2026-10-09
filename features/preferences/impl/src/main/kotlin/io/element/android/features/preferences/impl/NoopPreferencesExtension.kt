/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.preferences.api.PreferencesExtension
import io.element.android.libraries.di.SessionScope

@ContributesBinding(SessionScope::class)
class NoopPreferencesExtension : PreferencesExtension {
    @Composable
    override fun ColumnScope.Render(onNavigate: (String) -> Unit, modifier: Modifier) = Unit

    override fun createNode(parentNode: Node, buildContext: BuildContext, route: String): Node? = null
}
