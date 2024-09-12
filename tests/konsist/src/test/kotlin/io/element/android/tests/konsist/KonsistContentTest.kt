/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.konsist

import com.lemonappdev.konsist.api.Konsist
import com.lemonappdev.konsist.api.ext.list.withImportNamed
import com.lemonappdev.konsist.api.verify.assertFalse
import org.junit.Test

class KonsistContentTest {
    @Test
    fun `assert that BuildConfig dot VersionCode is not used`() {
        Konsist
            .scopeFromProduction()
            .files
            .withImportNamed("io.element.android.x.BuildConfig")
            .assertFalse(additionalMessage = "Please do not use BuildConfig.VERSION_CODE, but use the versionCode from BuildMeta") {
                it.text.contains("BuildConfig.VERSION_CODE")
            }
    }
}
