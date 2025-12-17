/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.detektrules

import com.google.common.truth.Truth.assertThat
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.test.compileAndLint
import org.junit.Test

class RunCatchingRuleTest {
    private val subject = RunCatchingRule(Config.empty)

    @Test
    fun `test RunCatchingRule`() {
        val findings = subject.compileAndLint(code)
        assertThat(findings).hasSize(3)
    }

    private val code = """
        object Foo {
            fun bar() {
                runCatching {}
                kotlin.runCatching {}
                Result.success(true).mapCatching { false }
            }
        }
        """.trimIndent()
}
