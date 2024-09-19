/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.di

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildType
import io.element.android.libraries.matrix.api.tracing.TracingFilterConfigurations
import io.element.android.libraries.matrix.test.core.aBuildMeta
import org.junit.Test

class TracingMatrixModuleTest {
    @Test
    fun `providesTracingFilterConfiguration returns debug config for debug build`() {
        assertThat(TracingMatrixModule.providesTracingFilterConfiguration(aBuildMeta(BuildType.DEBUG)))
            .isEqualTo(TracingFilterConfigurations.debug)
    }

    @Test
    fun `providesTracingFilterConfiguration returns nightly config for nightly build`() {
        assertThat(TracingMatrixModule.providesTracingFilterConfiguration(aBuildMeta(BuildType.NIGHTLY)))
            .isEqualTo(TracingFilterConfigurations.nightly)
    }

    @Test
    fun `providesTracingFilterConfiguration returns release config for release build`() {
        assertThat(TracingMatrixModule.providesTracingFilterConfiguration(aBuildMeta(BuildType.RELEASE)))
            .isEqualTo(TracingFilterConfigurations.release)
    }
}
