/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.file

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class FileTest {
    @Test
    fun `withFileNameSuffix - inserts the suffix before the extension`() {
        assertThat("image.png".withFileNameSuffix("_1")).isEqualTo("image_1.png")
    }

    @Test
    fun `withFileNameSuffix - appends the suffix when there is no extension`() {
        assertThat("README".withFileNameSuffix("_1")).isEqualTo("README_1")
    }

    @Test
    fun `withFileNameSuffix - only considers the last extension`() {
        assertThat("archive.tar.gz".withFileNameSuffix("_1")).isEqualTo("archive.tar_1.gz")
    }

    @Test
    fun `withFileNameSuffix - handles a name starting with a dot`() {
        assertThat(".hidden".withFileNameSuffix("_1")).isEqualTo("_1.hidden")
    }

    @Test
    fun `saveWithUniqueFileName - uses the original name when it works`() {
        val usedNames = mutableListOf<String>()

        val result = saveWithUniqueFileName(
            fileName = "image.png",
            uniqueSuffix = { "_1" },
        ) { name ->
            usedNames.add(name)
            "saved as $name"
        }

        assertThat(result).isEqualTo("saved as image.png")
        assertThat(usedNames).containsExactly("image.png")
    }

    @Test
    fun `saveWithUniqueFileName - retries with a suffixed name when the name cannot be made unique`() {
        val usedNames = mutableListOf<String>()

        val result = saveWithUniqueFileName(
            fileName = "image.png",
            uniqueSuffix = { "_1" },
        ) { name ->
            usedNames.add(name)
            if (name == "image.png") {
                error("Failed to build unique file")
            }
            "saved as $name"
        }

        assertThat(result).isEqualTo("saved as image_1.png")
        assertThat(usedNames).containsExactly("image.png", "image_1.png").inOrder()
    }

    @Test
    fun `saveWithUniqueFileName - does not retry more than once`() {
        val usedNames = mutableListOf<String>()

        val error = try {
            saveWithUniqueFileName(
                fileName = "image.png",
                uniqueSuffix = { "_1" },
            ) { name ->
                usedNames.add(name)
                error("Failed to build unique file")
            }
            null
        } catch (nameConflict: IllegalStateException) {
            nameConflict
        }

        assertThat(error).isInstanceOf(IllegalStateException::class.java)
        assertThat(usedNames).containsExactly("image.png", "image_1.png").inOrder()
    }
}
