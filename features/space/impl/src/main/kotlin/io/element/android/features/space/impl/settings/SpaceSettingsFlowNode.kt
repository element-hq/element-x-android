/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.settings

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.rolesandpermissions.api.RolesAndPermissionsEntryPoint
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyEntryPoint
import io.element.android.features.space.impl.di.SpaceFlowScope
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import kotlinx.parcelize.Parcelize

@ContributesNode(SpaceFlowScope::class)
@AssistedInject
class SpaceSettingsFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val securityAndPrivacyEntryPoint: SecurityAndPrivacyEntryPoint,
    private val rolesAndPermissionsEntryPoint: RolesAndPermissionsEntryPoint,
) : BaseFlowNode<SpaceSettingsFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.Root,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    interface Callback : Plugin {
        fun navigateToSpaceMembers()
        fun startLeaveSpaceFlow()
        fun closeSettings()
    }

    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Root : NavTarget

        @Parcelize
        data object SecurityAndPrivacy : NavTarget

        @Parcelize
        data object RolesAndPermissions : NavTarget
    }

    private val callback: Callback = callback()

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Root -> {
                val callback = object : SpaceSettingsNode.Callback {
                    override fun closeSettings() {
                        callback.closeSettings()
                    }

                    override fun navigateToEditDetails() {
                        // TODO
                    }

                    override fun navigateToSpaceMembers() {
                        callback.navigateToSpaceMembers()
                    }

                    override fun navigateToRolesAndPermissions() {
                        backstack.push(NavTarget.RolesAndPermissions)
                    }

                    override fun navigateToSecurityAndPrivacy() {
                        backstack.push(NavTarget.SecurityAndPrivacy)
                    }

                    override fun startLeaveSpaceFlow() {
                        callback.startLeaveSpaceFlow()
                    }
                }
                createNode<SpaceSettingsNode>(
                    buildContext = buildContext,
                    plugins = listOf(callback),
                )
            }
            is NavTarget.SecurityAndPrivacy -> {
                val callback = object : SecurityAndPrivacyEntryPoint.Callback {
                    override fun onDone() {
                        backstack.pop()
                    }
                }
                securityAndPrivacyEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                    callback = callback,
                )
            }
            is NavTarget.RolesAndPermissions -> {
                rolesAndPermissionsEntryPoint.createNode(
                    parentNode = this,
                    buildContext = buildContext,
                )
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(modifier)
    }
}
