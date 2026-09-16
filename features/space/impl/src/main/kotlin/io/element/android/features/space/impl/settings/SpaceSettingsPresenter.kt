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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import dev.zacsweers.metro.Inject
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.room.JoinedRoom
import io.element.android.libraries.matrix.api.room.powerlevels.permissionsAsState

@Inject
class SpaceSettingsPresenter(
    private val room: JoinedRoom,
) : Presenter<SpaceSettingsState> {
    @Composable
    override fun present(): SpaceSettingsState {
        val roomInfo by room.roomInfoFlow.collectAsState()
        val permissions by room.permissionsAsState(SpaceSettingsPermissions.DEFAULT) { perms ->
            perms.spaceSettingsPermissions()
        }
        val showSecurityAndPrivacy by remember {
            derivedStateOf { permissions.securityAndPrivacyPermissions.hasAny(isSpace = false, joinRule = roomInfo.joinRule) }
        }

        return SpaceSettingsState(
            roomId = room.roomId,
            name = roomInfo.name.orEmpty(),
            canonicalAlias = roomInfo.canonicalAlias,
            avatarUrl = roomInfo.avatarUrl,
            memberCount = roomInfo.activeMembersCount,
            canEditDetails = permissions.editDetailsPermissions.hasAny,
            showRolesAndPermissions = permissions.canEditRolesAndPermissions,
            showSecurityAndPrivacy = showSecurityAndPrivacy,
            eventSink = {},
        )
    }
}
