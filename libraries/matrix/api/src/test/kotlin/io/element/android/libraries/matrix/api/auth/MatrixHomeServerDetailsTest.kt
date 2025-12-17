/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.auth

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.test.auth.aMatrixHomeServerDetails
import org.junit.Test

class MatrixHomeServerDetailsTest {
    @Test
    fun `if homeserver supports oidc, then it is supported`() {
        val sut = aMatrixHomeServerDetails(
            supportsOidcLogin = true,
            supportsPasswordLogin = false,
        )
        assertThat(sut.isSupported).isTrue()
    }

    @Test
    fun `if homeserver supports password, then it is supported`() {
        val sut = aMatrixHomeServerDetails(
            supportsOidcLogin = false,
            supportsPasswordLogin = true,
        )
        assertThat(sut.isSupported).isTrue()
    }

    @Test
    fun `if homeserver supports both, then it is supported`() {
        val sut = aMatrixHomeServerDetails(
            supportsOidcLogin = true,
            supportsPasswordLogin = true,
        )
        assertThat(sut.isSupported).isTrue()
    }

    @Test
    fun `if homeserver supports none, then it is not supported`() {
        val sut = aMatrixHomeServerDetails(
            supportsOidcLogin = false,
            supportsPasswordLogin = false,
        )
        assertThat(sut.isSupported).isFalse()
    }
}
