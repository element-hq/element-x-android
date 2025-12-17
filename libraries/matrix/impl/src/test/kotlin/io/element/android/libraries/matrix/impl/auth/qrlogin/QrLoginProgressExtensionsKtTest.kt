/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth.qrlogin

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeLoginStep
import org.junit.Test
import org.matrix.rustcomponents.sdk.QrLoginProgress

class QrLoginProgressExtensionsKtTest {
    @Test
    fun `mapping QrLoginProgress should return expected result`() {
        assertThat(QrLoginProgress.Starting.toStep())
            .isEqualTo(QrCodeLoginStep.Starting)
        assertThat(QrLoginProgress.EstablishingSecureChannel(1u, "01").toStep())
            .isEqualTo(QrCodeLoginStep.EstablishingSecureChannel("01"))
        assertThat(QrLoginProgress.WaitingForToken("userCode").toStep())
            .isEqualTo(QrCodeLoginStep.WaitingForToken("userCode"))
        assertThat(QrLoginProgress.Done.toStep())
            .isEqualTo(QrCodeLoginStep.Finished)
    }
}
