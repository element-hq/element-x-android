/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.designsystem.showkase

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DesignSystemShowkaseRootModuleTest {
    /**
     * Showkase 1.0.5 omits the `SHOWKASE_CATEGORIES` destination from its nav graph when a single
     * category is populated, but the groups screen navigates to it on back press anyway and the app
     * crashes with `IllegalArgumentException: Navigation destination ... cannot be found`.
     *
     * Keeping at least two categories populated avoids that single-category graph. Components come
     * from the previews of this module, typography from the `@ShowkaseTypography` tokens of
     * `:libraries:compound` — which only reach us because that module applies the Showkase
     * processor.
     */
    @Test
    fun `at least two categories are populated, so the categories screen exists`() {
        val provider = DesignSystemShowkaseRootModuleCodegen()

        val populatedCategories = listOf(
            provider.getShowkaseComponents().size,
            provider.getShowkaseColors().size,
            provider.getShowkaseTypography().size,
        ).count { it > 0 }

        assertThat(populatedCategories).isAtLeast(2)
    }
}
