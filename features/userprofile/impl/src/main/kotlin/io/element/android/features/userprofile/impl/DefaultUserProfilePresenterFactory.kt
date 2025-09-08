/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.userprofile.impl

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import io.element.android.features.userprofile.api.UserProfilePresenterFactory
import io.element.android.features.userprofile.api.UserProfileState
import io.element.android.features.userprofile.impl.root.UserProfilePresenter
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.di.SessionScope
import io.element.android.libraries.matrix.api.core.UserId

@ContributesBinding(SessionScope::class)
@Inject
class DefaultUserProfilePresenterFactory(
    private val factory: UserProfilePresenter.Factory,
) : UserProfilePresenterFactory {
    override fun create(userId: UserId): Presenter<UserProfileState> = factory.create(userId)
}
