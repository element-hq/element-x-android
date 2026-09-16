/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.oauth.impl

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.oauth.api.OAuthAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Test

class DefaultOAuthActionFlowTest {
    @Test
    fun `collect gets all the posted events`() = runTest {
        val data = mutableListOf<OAuthAction?>()
        val sut = DefaultOAuthActionFlow()
        backgroundScope.launch {
            sut.collect { action ->
                data.add(action)
            }
        }
        sut.post(OAuthAction.GoBack())
        delay(1)
        sut.reset()
        delay(1)
        assertThat(data).containsExactly(OAuthAction.GoBack(), null)
    }
}
