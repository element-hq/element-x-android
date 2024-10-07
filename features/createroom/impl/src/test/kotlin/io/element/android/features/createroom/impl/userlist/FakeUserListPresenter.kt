/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.createroom.impl.userlist

import androidx.compose.runtime.Composable

class FakeUserListPresenter : UserListPresenter {
    private var state = aUserListState()

    fun givenState(state: UserListState) {
        this.state = state
    }

    @Composable
    override fun present(): UserListState {
        return state
    }
}
