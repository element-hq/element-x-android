/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.rolesandpermissions.impl.root

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.di.RoomScope

@ContributesNode(RoomScope::class)
@AssistedInject
class RolesAndPermissionsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: RolesAndPermissionsPresenter,
) : Node(buildContext, plugins = plugins), RolesAndPermissionsNavigator {
    interface Callback : Plugin, RolesAndPermissionsNavigator {
        override fun openAdminList()
        override fun openModeratorList()
        override fun openEditPermissions()

        override fun onBackClick() {}
    }

    private val callback: Callback = callback()

    @Stable
    private val navigator = object : RolesAndPermissionsNavigator by callback {
        override fun onBackClick() {
            navigateUp()
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        RolesAndPermissionsView(
            state = state,
            rolesAndPermissionsNavigator = navigator,
            modifier = modifier,
        )
    }
}

interface RolesAndPermissionsNavigator {
    fun onBackClick() {}
    fun openAdminList() {}
    fun openModeratorList() {}
    fun openEditPermissions() {}
}
