/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.securityandprivacy.impl

import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.pop
import com.bumble.appyx.navmodel.backstack.operation.push
import io.element.android.features.securityandprivacy.api.SecurityAndPrivacyEntryPoint

interface SecurityAndPrivacyNavigator : Plugin {
    fun onDone()
    fun openEditRoomAddress()
    fun closeEditRoomAddress()
    fun openManageAuthorizedSpaces()
    fun closeManageAuthorizedSpaces()
}

class BackstackSecurityAndPrivacyNavigator(
    private val callback: SecurityAndPrivacyEntryPoint.Callback,
    private val backStack: BackStack<SecurityAndPrivacyFlowNode.NavTarget>
) : SecurityAndPrivacyNavigator {
    override fun onDone() {
        callback.onDone()
    }

    override fun openEditRoomAddress() {
        backStack.push(SecurityAndPrivacyFlowNode.NavTarget.EditRoomAddress)
    }

    override fun closeEditRoomAddress() {
        backStack.pop()
    }

    override fun openManageAuthorizedSpaces() {
        backStack.push(SecurityAndPrivacyFlowNode.NavTarget.ManageAuthorizedSpaces)
    }

    override fun closeManageAuthorizedSpaces() {
        backStack.pop()
    }
}
