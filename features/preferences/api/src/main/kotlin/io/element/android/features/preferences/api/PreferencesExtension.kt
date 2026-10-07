/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.api

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node

/** Build-specific settings rows and child flows. Route IDs belong to the implementation and must be stable across restoration. */
interface PreferencesExtension {
    @Composable
    fun ColumnScope.Render(onNavigate: (String) -> Unit, modifier: Modifier)

    /** The returned flow owns its navigation and exits via navigateUp. Returns null for unsupported routes. */
    fun createNode(parentNode: Node, buildContext: BuildContext, route: String): Node?
}
