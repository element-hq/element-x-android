/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.deeplink.api.usecase

import android.app.Activity

/**
 * Opens the system share sheet with an invitation to install the app.
 */
fun interface InviteFriendsUseCase {
    /**
     * @param activity the activity the share sheet is shown from.
     */
    fun execute(activity: Activity)
}
