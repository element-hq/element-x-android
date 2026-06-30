/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.preferences.impl.store

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.preferences.api.store.UrlPreviewValue
import io.element.android.libraries.preferences.api.store.isUrlPreviewEnabled
import org.junit.Test

class UrlPreviewValueTest {
    @Test
    fun `On always enables previews`() {
        assertThat(UrlPreviewValue.On.isUrlPreviewEnabled(isEncrypted = true)).isTrue()
        assertThat(UrlPreviewValue.On.isUrlPreviewEnabled(isEncrypted = false)).isTrue()
    }

    @Test
    fun `Off always disables previews`() {
        assertThat(UrlPreviewValue.Off.isUrlPreviewEnabled(isEncrypted = true)).isFalse()
        assertThat(UrlPreviewValue.Off.isUrlPreviewEnabled(isEncrypted = false)).isFalse()
    }

    @Test
    fun `UnencryptedOnly enables previews only in unencrypted rooms`() {
        assertThat(UrlPreviewValue.UnencryptedOnly.isUrlPreviewEnabled(isEncrypted = false)).isTrue()
        assertThat(UrlPreviewValue.UnencryptedOnly.isUrlPreviewEnabled(isEncrypted = true)).isFalse()
    }

    @Test
    fun `null falls back to UnencryptedOnly behaviour`() {
        val value: UrlPreviewValue? = null
        assertThat(value.isUrlPreviewEnabled(isEncrypted = false)).isTrue()
        assertThat(value.isUrlPreviewEnabled(isEncrypted = true)).isFalse()
    }

    @Test
    fun `default is UnencryptedOnly`() {
        assertThat(UrlPreviewValue.DEFAULT).isEqualTo(UrlPreviewValue.UnencryptedOnly)
    }
}
