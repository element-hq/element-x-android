/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.permissions

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import java.io.File

/**
 * The Privacy Dashboard reaches [PermissionRationaleActivity] through a manifest declaration only, so
 * nothing else in the application would notice if it were dropped or exported without its permission guard.
 */
class PermissionRationaleActivityTest {
    private val manifest = File("src/main/AndroidManifest.xml").readText()

    @Test
    fun `the activity is declared`() {
        assertThat(manifest).contains("android:name=\".permissions.PermissionRationaleActivity\"")
    }

    @Test
    fun `the activity answers the system data access rationale intent`() {
        assertThat(manifest).contains("android.intent.action.VIEW_PERMISSION_USAGE")
    }

    @Test
    fun `only the permission controller can start the activity`() {
        assertThat(manifest).contains("android:permission=\"android.permission.START_VIEW_PERMISSION_USAGE\"")
    }
}
