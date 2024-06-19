/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
