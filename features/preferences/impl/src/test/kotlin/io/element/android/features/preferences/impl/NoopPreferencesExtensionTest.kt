/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.preferences.impl

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.google.common.truth.Truth.assertThat
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test

class NoopPreferencesExtensionTest : RobolectricTest() {
    @Test
    fun `FOSS extension has no rows or supported routes`() = runAndroidComposeUiTest<ComponentActivity> {
        val extension = NoopPreferencesExtension()
        setContent { Column { with(extension) { Render({ error("No navigation expected") }, Modifier) } } }
        onRoot().onChildren().assertCountEquals(0)
        val context = BuildContext.root(null)
        assertThat(extension.createNode(Node(context), context, "unavailable")).isNull()
    }
}
