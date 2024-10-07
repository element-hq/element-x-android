/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.permissions.test

import io.element.android.libraries.permissions.api.PermissionsPresenter

class FakePermissionsPresenterFactory(
    private val permissionPresenter: PermissionsPresenter = FakePermissionsPresenter(),
) : PermissionsPresenter.Factory {
    override fun create(permission: String): PermissionsPresenter {
        return permissionPresenter
    }
}
