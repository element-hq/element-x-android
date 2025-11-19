/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.mxc

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DefaultMxcToolsTest {
    @Test
    fun `mxcUri2FilePath returns extracted path`() {
        val mxcTools = DefaultMxcTools()
        val mxcUri = "mxc://server.org/abc123"
        val filePath = mxcTools.mxcUri2FilePath(mxcUri)
        assertThat(filePath).isEqualTo("server.org/abc123")
    }

    @Test
    fun `mxcUri2FilePath returns null for invalid data`() {
        val mxcTools = DefaultMxcTools()
        assertThat(mxcTools.mxcUri2FilePath("")).isNull()
        assertThat(mxcTools.mxcUri2FilePath("mxc://server.org")).isNull()
        assertThat(mxcTools.mxcUri2FilePath("mxc://server.org/")).isNull()
        assertThat(mxcTools.mxcUri2FilePath("m://server.org/abc123")).isNull()
    }
}
