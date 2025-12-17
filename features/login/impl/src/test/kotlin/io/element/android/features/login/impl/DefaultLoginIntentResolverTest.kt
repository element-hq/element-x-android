/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import io.element.android.features.login.api.LoginParams
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DefaultLoginIntentResolverTest {
    @Test
    fun `nominal case`() {
        val sut = DefaultLoginIntentResolver()
        val uriString = "https://mobile.element.io/element/?account_provider=example.org&login_hint=mxid:@alice:example.org"
        assertThat(sut.parse(uriString)).isEqualTo(
            LoginParams(
                accountProvider = "example.org",
                loginHint = "mxid:@alice:example.org",
            )
        )
    }

    @Test
    fun `extra unknown param`() {
        val sut = DefaultLoginIntentResolver()
        val uriString = "https://mobile.element.io/element/?account_provider=example.org&login_hint=mxid:@alice:example.org&extra=uknown"
        assertThat(sut.parse(uriString)).isEqualTo(
            LoginParams(
                accountProvider = "example.org",
                loginHint = "mxid:@alice:example.org",
            )
        )
    }

    @Test
    fun `no account provider`() {
        val sut = DefaultLoginIntentResolver()
        val uriString = "https://mobile.element.io/element/?login_hint=mxid:@alice:example.org"
        assertThat(sut.parse(uriString)).isNull()
    }

    @Test
    fun `no path`() {
        val sut = DefaultLoginIntentResolver()
        val uriString = "https://mobile.element.io?account_provider=example.org&login_hint=mxid:@alice:example.org"
        assertThat(sut.parse(uriString)).isNull()
    }

    @Test
    fun `wrong path`() {
        val sut = DefaultLoginIntentResolver()
        val uriString = "https://mobile.element.io/wrong?account_provider=example.org&login_hint=mxid:@alice:example.org"
        assertThat(sut.parse(uriString)).isNull()
    }

    @Test
    fun `wrong host`() {
        val sut = DefaultLoginIntentResolver()
        val uriString = "https://wrong.element.io/element/?account_provider=example.org&login_hint=mxid:@alice:example.org"
        assertThat(sut.parse(uriString)).isNull()
    }

    @Test
    fun `no login_hint param`() {
        val sut = DefaultLoginIntentResolver()
        val uriString = "https://mobile.element.io/element/?account_provider=example.org"
        assertThat(sut.parse(uriString)).isEqualTo(
            LoginParams(
                accountProvider = "example.org",
                loginHint = null,
            )
        )
    }
}
