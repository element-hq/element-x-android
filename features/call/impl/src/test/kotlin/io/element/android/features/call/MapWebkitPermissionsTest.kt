/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.call

import android.Manifest
import android.webkit.PermissionRequest
import com.google.common.truth.Truth.assertThat
import io.element.android.features.call.impl.ui.mapWebkitPermissions
import org.junit.Test

class MapWebkitPermissionsTest {
    @Test
    fun `given Webkit's RESOURCE_AUDIO_CAPTURE returns Android's RECORD_AUDIO permission`() {
        val permission = mapWebkitPermissions(arrayOf(PermissionRequest.RESOURCE_AUDIO_CAPTURE))
        assertThat(permission).isEqualTo(listOf(Manifest.permission.RECORD_AUDIO))
    }

    @Test
    fun `given Webkit's RESOURCE_VIDEO_CAPTURE returns Android's CAMERA permission`() {
        val permission = mapWebkitPermissions(arrayOf(PermissionRequest.RESOURCE_VIDEO_CAPTURE))
        assertThat(permission).isEqualTo(listOf(Manifest.permission.CAMERA))
    }

    @Test
    fun `given any other permission, it returns nothing`() {
        val permission = mapWebkitPermissions(arrayOf(PermissionRequest.RESOURCE_PROTECTED_MEDIA_ID))
        assertThat(permission).isEmpty()
    }
}
