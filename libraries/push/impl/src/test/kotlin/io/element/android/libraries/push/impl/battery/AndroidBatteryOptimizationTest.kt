/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.battery

import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import io.element.android.services.toolbox.api.intent.ExternalIntentLauncher
import io.element.android.services.toolbox.test.intent.FakeExternalIntentLauncher
import io.element.android.tests.testutils.lambda.lambdaRecorder
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AndroidBatteryOptimizationTest {
    @Test
    fun `isIgnoringBatteryOptimizations should return false`() {
        val sut = createAndroidBatteryOptimization()
        assertThat(sut.isIgnoringBatteryOptimizations()).isFalse()
    }

    @Test
    fun `requestDisablingBatteryOptimization is called once with expected intent`() {
        val launchLambda = lambdaRecorder<Intent, Unit> { intent ->
            assertThat(intent.action).isEqualTo(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            assertThat(intent.data.toString()).isEqualTo("package:${InstrumentationRegistry.getInstrumentation().context.packageName}")
        }
        val externalIntentLauncher = FakeExternalIntentLauncher(launchLambda)
        val sut = createAndroidBatteryOptimization(
            externalIntentLauncher = externalIntentLauncher,
        )
        val result = sut.requestDisablingBatteryOptimization()
        launchLambda.assertions().isCalledOnce()
        assertThat(result).isTrue()
    }

    @Test
    fun `in case of 1 error, requestDisablingBatteryOptimization returns true`() {
        var callNumber = 0
        val launchLambda = lambdaRecorder<Intent, Unit> { intent ->
            callNumber++
            when (callNumber) {
                1 -> {
                    assertThat(intent.action).isEqualTo(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    assertThat(intent.data.toString()).isEqualTo("package:${InstrumentationRegistry.getInstrumentation().context.packageName}")
                    throw ActivityNotFoundException("Test exception")
                }
                2 -> {
                    assertThat(intent.action).isEqualTo(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    assertThat(intent.data).isNull()
                    // No error
                }
                else -> {
                    throw AssertionError("Unexpected call number: $callNumber")
                }
            }
        }
        val externalIntentLauncher = FakeExternalIntentLauncher(launchLambda)
        val sut = createAndroidBatteryOptimization(
            externalIntentLauncher = externalIntentLauncher,
        )
        val result = sut.requestDisablingBatteryOptimization()
        launchLambda.assertions().isCalledExactly(2)
        assertThat(result).isTrue()
    }

    @Test
    fun `in case of 2 errors, requestDisablingBatteryOptimization returns false`() {
        var callNumber = 0
        val launchLambda = lambdaRecorder<Intent, Unit> { intent ->
            callNumber++
            when (callNumber) {
                1 -> {
                    assertThat(intent.action).isEqualTo(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                    assertThat(intent.data.toString()).isEqualTo("package:${InstrumentationRegistry.getInstrumentation().context.packageName}")
                    throw ActivityNotFoundException("Test exception")
                }
                2 -> {
                    assertThat(intent.action).isEqualTo(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                    assertThat(intent.data).isNull()
                    throw ActivityNotFoundException("Test exception")
                }
                else -> {
                    throw AssertionError("Unexpected call number: $callNumber")
                }
            }
        }
        val externalIntentLauncher = FakeExternalIntentLauncher(launchLambda)
        val sut = createAndroidBatteryOptimization(
            externalIntentLauncher = externalIntentLauncher,
        )
        val result = sut.requestDisablingBatteryOptimization()
        launchLambda.assertions().isCalledExactly(2)
        assertThat(result).isFalse()
    }

    private fun createAndroidBatteryOptimization(
        externalIntentLauncher: ExternalIntentLauncher = FakeExternalIntentLauncher(),
    ): AndroidBatteryOptimization {
        return AndroidBatteryOptimization(
            context = InstrumentationRegistry.getInstrumentation().context,
            externalIntentLauncher = externalIntentLauncher,
        )
    }
}
