/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class KonsistImportTest {
    @Test
    fun `Functions with '@VisibleForTesting' annotation should use 'androidx' version`() {
        Konsist
            .scopeFromProject()
            .imports
            .assertFalse(
                additionalMessage = "Please use 'androidx.annotation.VisibleForTesting' instead of " +
                    "'org.jetbrains.annotations.VisibleForTesting' (project convention).",
            ) {
                it.name == "org.jetbrains.annotations.VisibleForTesting"
            }
    }

    @Test
    fun `OutlinedTextField should not be used`() {
        Konsist
            .scopeFromProject()
            .imports
            .assertFalse(
                additionalMessage = "Please use 'io.element.android.libraries.designsystem.theme.components.TextField' instead of " +
                    "'androidx.compose.material3.OutlinedTextField.",
            ) {
                it.name == "androidx.compose.material3.OutlinedTextField"
            }
    }

    @Test
    fun `material3 TopAppBar should not be used`() {
        Konsist
            .scopeFromProject()
            .imports
            .assertFalse(
                additionalMessage = "Please use 'io.element.android.libraries.designsystem.theme.components.TopAppBar' instead of " +
                    "'androidx.compose.material3.TopAppBar.",
            ) {
                it.name == "androidx.compose.material3.TopAppBar"
            }
    }
}
