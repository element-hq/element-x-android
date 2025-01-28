/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.roomdetails.impl.securityandprivacy.editroomaddress

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.roomdetails.impl.securityandprivacy.SecurityAndPrivacyNavigator
import io.element.android.libraries.di.RoomScope

@ContributesNode(RoomScope::class)
class EditRoomAddressNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    presenterFactory: EditRoomAddressPresenter.Factory,
) : Node(buildContext, plugins = plugins) {
    private val navigator = plugins<SecurityAndPrivacyNavigator>().first()
    private val presenter = presenterFactory.create(navigator)

    @Composable
    override fun View(modifier: Modifier) {
        val state = presenter.present()
        EditRoomAddressView(
            state = state,
            onBackClick = ::navigateUp,
            modifier = modifier
        )
    }
}
