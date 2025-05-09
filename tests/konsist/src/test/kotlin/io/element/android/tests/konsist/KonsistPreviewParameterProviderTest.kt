/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.konsist

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withAllParentsOf
import com.lemonappdev.konsist.api.verify.assertTrue
import org.junit.Test

class KonsistPreviewParameterProviderTest {
    @Test
    fun `Classes extending 'PreviewParameterProvider' must be in their own file`() {
        Konsist.scopeFromProduction()
            .classes()
            .withAllParentsOf(PreviewParameterProvider::class)
            .assertTrue { klass ->
                klass.containingFile.name == klass.name
            }
    }
}
