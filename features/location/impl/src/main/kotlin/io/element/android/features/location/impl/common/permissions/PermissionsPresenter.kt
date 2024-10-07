/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.location.impl.common.permissions

import io.element.android.libraries.architecture.Presenter

interface PermissionsPresenter : Presenter<PermissionsState> {
    interface Factory {
        fun create(permissions: List<String>): PermissionsPresenter
    }
}
