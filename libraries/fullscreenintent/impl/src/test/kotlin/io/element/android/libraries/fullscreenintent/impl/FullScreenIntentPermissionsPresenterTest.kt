/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.fullscreenintent.impl

import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Lifecycle
import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.core.meta.BuildMeta
import io.element.android.libraries.fullscreenintent.api.FullScreenIntentPermissionsEvent
import io.element.android.libraries.matrix.test.core.aBuildMeta
import io.element.android.libraries.preferences.test.FakePreferenceDataStoreFactory
import io.element.android.services.toolbox.api.intent.ExternalIntentLauncher
import io.element.android.services.toolbox.test.intent.FakeExternalIntentLauncher
import io.element.android.services.toolbox.test.sdk.FakeBuildVersionSdkIntProvider
import io.element.android.tests.testutils.FakeLifecycleOwner
import io.element.android.tests.testutils.WarmUpRule
import io.element.android.tests.testutils.lambda.lambdaRecorder
import io.element.android.tests.testutils.testWithLifecycleOwner
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FullScreenIntentPermissionsPresenterTest {
    @get:Rule
    val warmUpRule = WarmUpRule()

    @Test
    fun `shouldDisplay - is true when permission is not granted and banner is not dismissed`() = runTest {
        val presenter = createPresenter(
            notificationManagerCompat = mockk {
                every { canUseFullScreenIntent() } returns false
            }
        )
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val initialItem = awaitItem()
            assertThat(initialItem.shouldDisplayBanner).isTrue()
        }
    }

    @Test
    fun `shouldDisplay - is false if permission is granted`() = runTest {
        val presenter = createPresenter(
            notificationManagerCompat = mockk {
                every { canUseFullScreenIntent() } returns true
            }
        )
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val initialItem = awaitItem()
            assertThat(initialItem.shouldDisplayBanner).isFalse()
        }
    }

    @Test
    fun `shouldDisplay - becomes false when permission is granted while the app is in background`() = runTest {
        var canUseFullScreenIntent = false
        val presenter = createPresenter(
            notificationManagerCompat = mockk {
                every { canUseFullScreenIntent() } answers { canUseFullScreenIntent }
            }
        )
        val lifecycleOwner = FakeLifecycleOwner(Lifecycle.State.RESUMED)
        presenter.testWithLifecycleOwner(lifecycleOwner) {
            skipItems(1)
            assertThat(awaitItem().shouldDisplayBanner).isTrue()
            // User opens the system settings and grants the permission
            lifecycleOwner.givenState(Lifecycle.State.STARTED)
            canUseFullScreenIntent = true
            lifecycleOwner.givenState(Lifecycle.State.RESUMED)
            val finalItem = awaitItem()
            assertThat(finalItem.permissionGranted).isTrue()
            assertThat(finalItem.shouldDisplayBanner).isFalse()
        }
    }

    @Test
    fun `dismissFullScreenIntentBanner - makes shouldDisplay false`() = runTest {
        val presenter = createPresenter()
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val loadedItem = awaitItem()
            loadedItem.eventSink(FullScreenIntentPermissionsEvent.Dismiss)
            runCurrent()
            assertThat(awaitItem().shouldDisplayBanner).isFalse()
        }
    }

    @Test
    fun `openFullScreenIntentSettings - opens external screen using intent`() = runTest {
        val launchLambda = lambdaRecorder<Intent, Unit> { _ -> }
        val externalIntentLauncher = FakeExternalIntentLauncher(launchLambda)
        val presenter = createPresenter(externalIntentLauncher = externalIntentLauncher)
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val loadedItem = awaitItem()
            loadedItem.eventSink(FullScreenIntentPermissionsEvent.OpenSettings)
            launchLambda.assertions().isCalledOnce()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `openFullScreenIntentSettings - does nothing in old APIs`() = runTest {
        val launchLambda = lambdaRecorder<Intent, Unit> { _ -> }
        val externalIntentLauncher = FakeExternalIntentLauncher(launchLambda)
        val presenter = createPresenter(
            buildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(Build.VERSION_CODES.Q),
            externalIntentLauncher = externalIntentLauncher,
        )
        presenter.testWithLifecycleOwner {
            skipItems(1)
            val loadedItem = awaitItem()
            loadedItem.eventSink(FullScreenIntentPermissionsEvent.OpenSettings)
            launchLambda.assertions().isNeverCalled()
            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun createPresenter(
        buildVersionSdkIntProvider: FakeBuildVersionSdkIntProvider = FakeBuildVersionSdkIntProvider(Build.VERSION_CODES.UPSIDE_DOWN_CAKE),
        dataStoreFactory: FakePreferenceDataStoreFactory = FakePreferenceDataStoreFactory(),
        externalIntentLauncher: ExternalIntentLauncher = FakeExternalIntentLauncher(),
        buildMeta: BuildMeta = aBuildMeta(),
        notificationManagerCompat: NotificationManagerCompat = mockk(relaxed = true)
    ) = FullScreenIntentPermissionsPresenter(
        buildVersionSdkIntProvider = buildVersionSdkIntProvider,
        externalIntentLauncher = externalIntentLauncher,
        buildMeta = buildMeta,
        preferencesDataStoreFactory = dataStoreFactory,
        notificationManagerCompat = notificationManagerCompat,
    )
}
