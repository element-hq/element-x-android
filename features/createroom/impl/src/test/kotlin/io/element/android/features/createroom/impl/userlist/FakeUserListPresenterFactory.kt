/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.createroom.impl.userlist

import io.element.android.libraries.usersearch.api.UserRepository

class FakeUserListPresenterFactory(
    private val fakeUserListPresenter: FakeUserListPresenter = FakeUserListPresenter()
) : UserListPresenter.Factory {
    override fun create(
        args: UserListPresenterArgs,
        userRepository: UserRepository,
        userListDataStore: UserListDataStore,
    ): UserListPresenter = fakeUserListPresenter
}
