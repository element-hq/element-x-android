/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.space.impl.settings

import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId

data class SpaceSettingsState(
    val roomId: RoomId,
    val name: String,
    val canonicalAlias: RoomAlias?,
    val avatarUrl: String?,
    val memberCount: Long,
    val canEditDetails: Boolean,
    val showRolesAndPermissions: Boolean,
    val showSecurityAndPrivacy: Boolean,
    val eventSink: (SpaceSettingsEvents) -> Unit
)
