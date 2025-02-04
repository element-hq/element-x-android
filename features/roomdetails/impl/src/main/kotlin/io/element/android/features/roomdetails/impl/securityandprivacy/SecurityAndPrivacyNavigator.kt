/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy

import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push

interface SecurityAndPrivacyNavigator : Plugin {
    fun openEditRoomAddress()
    fun closeEditRoomAddress()
}

class BackstackSecurityAndPrivacyNavigator(
    private val backStack: BackStack<SecurityAndPrivacyFlowNode.NavTarget>
) : SecurityAndPrivacyNavigator {
    override fun openEditRoomAddress() {
        backStack.push(SecurityAndPrivacyFlowNode.NavTarget.EditRoomAddress)
    }

    override fun closeEditRoomAddress() {
        backStack.pop()
    }
}
