/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.user.editprofile

import io.element.android.libraries.matrix.ui.media.AvatarAction

sealed interface EditUserProfileEvents {
    data class HandleAvatarAction(val action: AvatarAction) : EditUserProfileEvents
    data class UpdateDisplayName(val name: String) : EditUserProfileEvents
    data object Exit : EditUserProfileEvents
    data object Save : EditUserProfileEvents
    data object CloseDialog : EditUserProfileEvents
}
