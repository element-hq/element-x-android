/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package extension

import com.android.build.api.variant.DeviceTestBuilder
import com.android.build.api.variant.HasDeviceTestsBuilder
import com.android.build.api.variant.HasHostTestsBuilder
import com.android.build.api.variant.HostTestBuilder
import com.android.build.api.variant.VariantBuilder
import org.gradle.api.Project

/**
 * Only create the test components that have something to run.
 *
 * By default, AGP creates a unit test and an androidTest component for every variant of every module, each with its own
 * set of tasks to configure, even when the module has no test sources. Here:
 * - unit tests are only created for the debug build type (the only one run by the CI), and only if the module has unit tests;
 * - androidTests are only created if the module has instrumentation tests.
 */
fun Project.configureTestComponents(variantBuilder: VariantBuilder) {
    val hasUnitTests = file("src/test").exists()
    val hasDeviceTests = file("src/androidTest").exists()
    (variantBuilder as? HasHostTestsBuilder)?.hostTests?.get(HostTestBuilder.UNIT_TEST_TYPE)?.enable =
        hasUnitTests && variantBuilder.buildType == "debug"
    (variantBuilder as? HasDeviceTestsBuilder)?.deviceTests?.get(DeviceTestBuilder.ANDROID_TEST_TYPE)?.enable = hasDeviceTests
}
