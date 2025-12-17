/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.userprofile.impl

import dev.zacsweers.metro.ContributesBinding
import io.element.android.features.userprofile.api.UserProfilePresenterFactory
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.impl.root.UserProfilePresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.UserId

@ContributesBinding(SessionScope::class)
class DefaultUserProfilePresenterFactory(
    private val factory: UserProfilePresenter.Factory,
) : UserProfilePresenterFactory {
    override fun create(userId: UserId): Presenter<UserProfileState> = factory.create(userId)
}
