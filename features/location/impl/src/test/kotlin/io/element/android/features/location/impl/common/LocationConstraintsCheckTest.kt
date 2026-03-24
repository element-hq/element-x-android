/*
 * Copyright (c) 2025 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.location.impl.common

import com.google.common.truth.Truth.assertThat
import io.element.android.features.location.impl.aPermissionsState
import io.element.android.features.location.impl.common.actions.FakeLocationActions
import io.element.android.features.location.impl.common.permissions.PermissionsState
import org.junit.Test

class LocationConstraintsCheckTest {
    @Test
    fun `checkLocationConstraints returns Success when permissions granted and location enabled`() {
        val permissionsState = aPermissionsState(
            permissions = PermissionsState.Permissions.AllGranted,
        )
        val locationActions = FakeLocationActions(isLocationEnabled = true)

        val result = checkLocationConstraints(permissionsState, locationActions)

        assertThat(result).isEqualTo(LocationConstraintsCheck.Success)
    }

    @Test
    fun `checkLocationConstraints returns Success when some permissions granted and location enabled`() {
        val permissionsState = aPermissionsState(
            permissions = PermissionsState.Permissions.SomeGranted,
        )
        val locationActions = FakeLocationActions(isLocationEnabled = true)

        val result = checkLocationConstraints(permissionsState, locationActions)

        assertThat(result).isEqualTo(LocationConstraintsCheck.Success)
    }

    @Test
    fun `checkLocationConstraints returns LocationServiceDisabled when permissions granted but location disabled`() {
        val permissionsState = aPermissionsState(
            permissions = PermissionsState.Permissions.AllGranted,
        )
        val locationActions = FakeLocationActions(isLocationEnabled = false)

        val result = checkLocationConstraints(permissionsState, locationActions)

        assertThat(result).isEqualTo(LocationConstraintsCheck.LocationServiceDisabled)
    }

    @Test
    fun `checkLocationConstraints returns PermissionRationale when permissions denied with rationale`() {
        val permissionsState = aPermissionsState(
            permissions = PermissionsState.Permissions.NoneGranted,
            shouldShowRationale = true,
        )
        val locationActions = FakeLocationActions(isLocationEnabled = true)

        val result = checkLocationConstraints(permissionsState, locationActions)

        assertThat(result).isEqualTo(LocationConstraintsCheck.PermissionRationale)
    }

    @Test
    fun `checkLocationConstraints returns PermissionDenied when permissions denied without rationale`() {
        val permissionsState = aPermissionsState(
            permissions = PermissionsState.Permissions.NoneGranted,
            shouldShowRationale = false,
        )
        val locationActions = FakeLocationActions(isLocationEnabled = true)

        val result = checkLocationConstraints(permissionsState, locationActions)

        assertThat(result).isEqualTo(LocationConstraintsCheck.PermissionDenied)
    }
}
