/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oidc.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.oidc.api.OidcAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultOidcActionFlowTest {
    @Test
    fun `collect gets all the posted events`() = runTest {
        val data = mutableListOf<OidcAction?>()
        val sut = DefaultOidcActionFlow()
        backgroundScope.launch {
            sut.collect { action ->
                data.add(action)
            }
        }
        sut.post(OidcAction.GoBack())
        delay(1)
        sut.reset()
        delay(1)
        assertThat(data).containsExactly(OidcAction.GoBack(), null)
    }
}
