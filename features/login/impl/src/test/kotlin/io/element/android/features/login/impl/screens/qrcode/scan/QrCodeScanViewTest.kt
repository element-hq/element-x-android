/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.login.impl.screens.qrcode.scan

import androidx.activity.ComponentActivity
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.platform.app.InstrumentationRegistry
import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.matrix.api.auth.qrlogin.MatrixQrCodeLoginData
import io.element.android.libraries.matrix.test.auth.qrlogin.FakeMatrixQrCodeLoginData
import io.element.android.libraries.permissions.api.localnetwork.LocalNetworkPermissionDialog
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.tests.testutils.EnsureNeverCalled
import io.element.android.tests.testutils.EnsureNeverCalledWithParam
import io.element.android.tests.testutils.EventsRecorder
import io.element.android.tests.testutils.clickOn
import io.element.android.tests.testutils.ensureCalledOnce
import io.element.android.tests.testutils.ensureCalledOnceWithParam
import io.element.android.tests.testutils.pressBackKey
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class QrCodeScanViewTest : RobolectricTest() {
    private var provider: ProcessCameraProvider? = null

    @Before
    fun setup() {
        val context = InstrumentationRegistry.getInstrumentation().context
        provider = ProcessCameraProvider.getInstance(context).get()
    }

    @After
    fun teardown() {
        provider?.unbindAll()
    }

    @Test
    fun `on back pressed - calls the expected callback`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeScanView(
                state = aQrCodeScanState(),
                onBackClick = callback
            )
            pressBackKey()
        }
    }

    @Test
    fun `on QR code data ready and can proceed - calls the expected callback`() = runAndroidComposeUiTest {
        val data = FakeMatrixQrCodeLoginData()
        ensureCalledOnceWithParam<MatrixQrCodeLoginData>(data) { callback ->
            setQrCodeScanView(
                state = aQrCodeScanState(
                    authenticationAction = AsyncAction.Success(QrCodeScanResult(data = data, canProceed = true)),
                ),
                onQrCodeDataReady = callback
            )
        }
    }

    @Test
    fun `on QR code data ready but cannot proceed - requests the local network access permission`() = runAndroidComposeUiTest {
        val data = FakeMatrixQrCodeLoginData()
        val eventRecorder = EventsRecorder<QrCodeScanEvent>()
        setQrCodeScanView(
            state = aQrCodeScanState(
                authenticationAction = AsyncAction.Success(QrCodeScanResult(data = data, canProceed = false)),
                eventSink = eventRecorder,
            ),
        )
        eventRecorder.assertSingle(QrCodeScanEvent.RequestLocalNetworkAccessPermission)
    }

    @Test
    fun `on local homeserver access denied - navigates back`() = runAndroidComposeUiTest {
        ensureCalledOnce { callback ->
            setQrCodeScanView(
                state = aQrCodeScanState(
                    authenticationAction = AsyncAction.Failure(CannotAccessLocalHomeserverException()),
                ),
                onBackClick = callback
            )
        }
    }

    @Test
    fun `on local network permission dialog submit - emits the request permission event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<QrCodeScanEvent>()
        setQrCodeScanView(
            state = aQrCodeScanState(
                localNetworkPermissionDialog = LocalNetworkPermissionDialog.Rationale,
                eventSink = eventRecorder,
            ),
        )
        clickOn(CommonStrings.dialog_allow_access)
        eventRecorder.assertSingle(QrCodeScanEvent.RequestLocalNetworkAccessPermission)
    }

    @Test
    fun `on local network permission dialog dismiss - emits the dismiss event`() = runAndroidComposeUiTest {
        val eventRecorder = EventsRecorder<QrCodeScanEvent>()
        setQrCodeScanView(
            state = aQrCodeScanState(
                localNetworkPermissionDialog = LocalNetworkPermissionDialog.Rationale,
                eventSink = eventRecorder,
            ),
        )
        clickOn(CommonStrings.action_not_now)
        eventRecorder.assertSingle(QrCodeScanEvent.DismissLocalNetworkAccessPermissionDialog)
    }

    private fun AndroidComposeUiTest<ComponentActivity>.setQrCodeScanView(
        state: QrCodeScanState,
        onBackClick: () -> Unit = EnsureNeverCalled(),
        onQrCodeDataReady: (MatrixQrCodeLoginData) -> Unit = EnsureNeverCalledWithParam(),
    ) {
        setContent {
            QrCodeScanView(
                state = state,
                onBackClick = onBackClick,
                onQrCodeDataReady = onQrCodeDataReady
            )
        }
    }
}
