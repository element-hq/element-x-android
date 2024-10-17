/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.userprofile.api

import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.matrix.api.core.UserId

fun interface UserProfilePresenterFactory {
    fun create(userId: UserId): Presenter<UserProfileState>
}
