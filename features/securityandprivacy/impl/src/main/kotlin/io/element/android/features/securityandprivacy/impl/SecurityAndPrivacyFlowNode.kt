/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import io.element.android.annotations.ContributesNode
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyEntryPoint
import io.element.android.features.securityandprivacy.api.securityAndPrivacyPermissions
import io.element.android.features.securityandprivacy.impl.editroomaddress.EditRoomAddressNode
import io.element.android.features.securityandprivacy.impl.root.SecurityAndPrivacyNode
import io.element.android.libraries.architecture.BackstackView
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.callback
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.powerlevels.use
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
@AssistedInject
class SecurityAndPrivacyFlowNode(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val room: JoinedRoom,
) : BaseFlowNode<SecurityAndPrivacyFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = NavTarget.SecurityAndPrivacy,
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins,
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object SecurityAndPrivacy : NavTarget

        @Parcelize
        data object EditRoomAddress : NavTarget
    }

    private val callback: SecurityAndPrivacyEntryPoint.Callback = callback()
    private val navigator = BackstackSecurityAndPrivacyNavigator(callback, backstack)

    override fun onBuilt() {
        super.onBuilt()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                room.roomInfoFlow
                    .map { roomInfo ->
                        room.roomPermissions().use(false) { perms ->
                            perms.securityAndPrivacyPermissions().hasAny(roomInfo.isSpace, roomInfo.joinRule)
                        }
                    }
                    .filter { canEdit -> !canEdit }
                    .first()
                // If the user can no longer edit security and privacy, exit the flow
                callback.onDone()
            }
        }
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            NavTarget.SecurityAndPrivacy -> {
                createNode<SecurityAndPrivacyNode>(buildContext, plugins = listOf(navigator))
            }
            NavTarget.EditRoomAddress -> {
                createNode<EditRoomAddressNode>(buildContext, plugins = listOf(navigator))
            }
        }
    }

    @Composable
    override fun View(modifier: Modifier) {
        BackstackView(modifier)
    }
}
