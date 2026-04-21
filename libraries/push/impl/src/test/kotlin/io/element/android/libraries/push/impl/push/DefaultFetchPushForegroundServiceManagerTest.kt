/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.push

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context.ACTIVITY_SERVICE
import android.content.Context.POWER_SERVICE
import android.os.PowerManager
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeout
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowActivityManager
import org.robolectric.shadows.ShadowPowerManager
import kotlin.time.Duration.Companion.seconds

@RunWith(AndroidJUnit4::class)
class DefaultFetchPushForegroundServiceManagerTest {
    @Test
    fun `start should start the service if the device is not interactive`() {
        val manager = createDefaultFetchPushForegroundServiceManager()

        getShadowPowerManager().turnScreenOn(false)

        assertThat(manager.start()).isTrue()
    }

    @Test
    fun `start won't start the service if the device is interactive`() {
        val manager = createDefaultFetchPushForegroundServiceManager()

        getShadowPowerManager().turnScreenOn(true)

        assertThat(manager.start()).isFalse()
    }

    @Test
    fun `stop will stop the service if it's running`() = runTest {
        val manager = createDefaultFetchPushForegroundServiceManager()

        // Start the service first
        getShadowPowerManager().turnScreenOn(false)
        manager.start()

        getShadowActivityManager().setServices(
            listOf(
                ActivityManager.RunningServiceInfo().apply {
                    service = ComponentName(InstrumentationRegistry.getInstrumentation().context, FetchPushForegroundService::class.java)
                    foreground = true
                }
            )
        )

        assertThat(manager.stop()).isTrue()
    }

    @Test
    fun `stop will eventually stop the service once it's on foreground`() = runTest {
        val manager = createDefaultFetchPushForegroundServiceManager()

        // Start the service first
        getShadowPowerManager().turnScreenOn(false)
        manager.start()

        // The service is started, but not yet in foreground
        getShadowActivityManager().setServices(
            listOf(
                ActivityManager.RunningServiceInfo().apply {
                    service = ComponentName(InstrumentationRegistry.getInstrumentation().context, FetchPushForegroundService::class.java)
                    foreground = false
                }
            )
        )

        // We call stop, which won't stop the service yet since it's not in foreground
        val future = async { manager.stop() }

        // Then we set the service as running in foreground, which should allow the stop to complete
        getShadowActivityManager().setServices(
            listOf(
                ActivityManager.RunningServiceInfo().apply {
                    service = ComponentName(InstrumentationRegistry.getInstrumentation().context, FetchPushForegroundService::class.java)
                    foreground = true
                }
            )
        )

        val stopped = withTimeout(5.seconds) { future.await() }
        assertThat(stopped).isTrue()
    }

    @Test
    fun `stop will not stop the service if it's stopped`() = runTest {
        val manager = createDefaultFetchPushForegroundServiceManager()

        // Set some fake running service data, even if the service is not really running
        getShadowActivityManager().setServices(
            listOf(
                ActivityManager.RunningServiceInfo().apply {
                    service = ComponentName(InstrumentationRegistry.getInstrumentation().context, FetchPushForegroundService::class.java)
                    foreground = true
                }
            )
        )

        // Since the service was not really running, it was not stopped
        assertThat(manager.stop()).isFalse()
    }

    private fun createDefaultFetchPushForegroundServiceManager() = DefaultFetchPushForegroundServiceManager(
        context = InstrumentationRegistry.getInstrumentation().context,
    )

    private fun getShadowPowerManager(): ShadowPowerManager {
        val powerManager = InstrumentationRegistry.getInstrumentation().context.getSystemService(POWER_SERVICE) as PowerManager
        return Shadows.shadowOf(powerManager)
    }

    private fun getShadowActivityManager(): ShadowActivityManager {
        val activityManager = InstrumentationRegistry.getInstrumentation().context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        return Shadows.shadowOf(activityManager)
    }
}
