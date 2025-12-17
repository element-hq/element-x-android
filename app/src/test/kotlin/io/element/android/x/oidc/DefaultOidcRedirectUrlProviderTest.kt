/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x.oidc

import com.google.common.truth.Truth.assertThat
import io.element.android.services.toolbox.test.strings.FakeStringProvider
import io.element.android.x.R
import org.junit.Test

class DefaultOidcRedirectUrlProviderTest {
    @Test
    fun `test provide`() {
        val stringProvider = FakeStringProvider(
            defaultResult = "str"
        )
        val sut = DefaultOidcRedirectUrlProvider(
            stringProvider = stringProvider,
        )
        val result = sut.provide()
        assertThat(result).isEqualTo("str:/")
        assertThat(stringProvider.lastResIdParam).isEqualTo(R.string.login_redirect_scheme)
    }
}
