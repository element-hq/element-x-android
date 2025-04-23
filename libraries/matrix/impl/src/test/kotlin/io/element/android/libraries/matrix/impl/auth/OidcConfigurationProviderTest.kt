/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.auth

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.auth.OidcConfig
import io.element.android.libraries.matrix.test.core.aBuildMeta
import org.junit.Test

class OidcConfigurationProviderTest {
    @Test
    fun get() {
        val result = OidcConfigurationProvider(
            aBuildMeta(
                applicationName = "myName",
            )
        ).get()
        assertThat(result.clientName).isEqualTo("myName")
        assertThat(result.redirectUri).isEqualTo(OidcConfig.REDIRECT_URI)
    }
}
