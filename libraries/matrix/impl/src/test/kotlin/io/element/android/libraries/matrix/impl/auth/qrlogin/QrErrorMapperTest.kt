/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth.qrlogin

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.qrlogin.QrCodeDecodeException
import io.element.android.libraries.matrix.api.auth.qrlogin.QrLoginException
import org.junit.Test
import org.matrix.rustcomponents.sdk.HumanQrLoginException as RustHumanQrLoginException
import org.matrix.rustcomponents.sdk.QrCodeDecodeException as RustQrCodeDecodeException

class QrErrorMapperTest {
    @Test
    fun `test map QrCodeDecodeException`() {
        val result = QrErrorMapper.map(RustQrCodeDecodeException.Crypto("test"))
        assertThat(result).isInstanceOf(QrCodeDecodeException.Crypto::class.java)
        assertThat(result.message).isEqualTo("test")
    }

    @Test
    fun `test map HumanQrLoginException`() {
        assertThat(QrErrorMapper.map(RustHumanQrLoginException.Cancelled())).isEqualTo(QrLoginException.Cancelled)
        assertThat(QrErrorMapper.map(RustHumanQrLoginException.ConnectionInsecure())).isEqualTo(QrLoginException.ConnectionInsecure)
        assertThat(QrErrorMapper.map(RustHumanQrLoginException.Declined())).isEqualTo(QrLoginException.Declined)
        assertThat(QrErrorMapper.map(RustHumanQrLoginException.Expired())).isEqualTo(QrLoginException.Expired)
        assertThat(QrErrorMapper.map(RustHumanQrLoginException.OtherDeviceNotSignedIn())).isEqualTo(QrLoginException.OtherDeviceNotSignedIn)
        assertThat(QrErrorMapper.map(RustHumanQrLoginException.LinkingNotSupported())).isEqualTo(QrLoginException.LinkingNotSupported)
        assertThat(QrErrorMapper.map(RustHumanQrLoginException.Unknown())).isEqualTo(QrLoginException.Unknown)
        assertThat(QrErrorMapper.map(RustHumanQrLoginException.OidcMetadataInvalid())).isEqualTo(QrLoginException.OidcMetadataInvalid)
        assertThat(QrErrorMapper.map(RustHumanQrLoginException.SlidingSyncNotAvailable())).isEqualTo(QrLoginException.SlidingSyncNotAvailable)
    }
}
