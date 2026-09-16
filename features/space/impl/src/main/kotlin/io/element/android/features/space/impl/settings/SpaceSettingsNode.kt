/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.space.impl.di.SpaceFlowScope
import io.element.android.libraries.architecture.appyx.launchMolecule
import io.element.android.libraries.architecture.callback

@ContributesNode(SpaceFlowScope::class)
@AssistedInject
class SpaceSettingsNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val presenter: SpaceSettingsPresenter,
) : Node(buildContext, plugins = plugins) {
    interface Callback : Plugin {
        fun closeSettings()

        fun navigateToEditDetails()
        fun navigateToSpaceMembers()
        fun navigateToRolesAndPermissions()
        fun navigateToSecurityAndPrivacy()
        fun startLeaveSpaceFlow()
    }

    private val callback: Callback = callback()
    private val stateFlow = launchMolecule { presenter.present() }

    @Composable
    override fun View(modifier: Modifier) {
        val state by stateFlow.collectAsState()
        SpaceSettingsView(
            state = state,
            modifier = modifier,
            onSpaceInfoClick = callback::navigateToEditDetails,
            onBackClick = callback::closeSettings,
            onMembersClick = callback::navigateToSpaceMembers,
            onRolesAndPermissionsClick = callback::navigateToRolesAndPermissions,
            onSecurityAndPrivacyClick = callback::navigateToSecurityAndPrivacy,
            onLeaveSpaceClick = callback::startLeaveSpaceFlow,
        )
    }
}
