/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.chooseaccountprovider

import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.impl.accountprovider.anAccountProvider
import io.element.android.features.login.impl.login.LoginMode
import io.element.android.libraries.architecture.AsyncData
import org.junit.Test

class ChooseAccountProviderStateTest {
    @Test
    fun `submitEnabled returns false when there is no selectedAccountProvider`() {
        val sut = aChooseAccountProviderState(
            selectedAccountProvider = null,
        )
        assertThat(sut.submitEnabled).isFalse()
    }

    @Test
    fun `submitEnabled returns true when there is a selectedAccountProvider`() {
        val sut = aChooseAccountProviderState(
            selectedAccountProvider = anAccountProvider(),
        )
        assertThat(sut.submitEnabled).isTrue()
    }

    @Test
    fun `submitEnabled returns false when there is a selectedAccountProvider but there is an error`() {
        val sut = aChooseAccountProviderState(
            selectedAccountProvider = anAccountProvider(),
            loginMode = AsyncData.Failure(Throwable("Error")),
        )
        assertThat(sut.submitEnabled).isFalse()
    }

    @Test
    fun `submitEnabled returns false when there is a selectedAccountProvider but the result is successful`() {
        val sut = aChooseAccountProviderState(
            selectedAccountProvider = anAccountProvider(),
            loginMode = AsyncData.Success(LoginMode.PasswordLogin),
        )
        assertThat(sut.submitEnabled).isFalse()
    }
}
