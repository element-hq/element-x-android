/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.appupdate.impl

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.json.Json
import org.junit.Test

class UpdateManifestTest {
    private val json = Json { ignoreUnknownKeys = true }

    private val sampleJson = """
        {
          "schema": 1,
          "versionName": "26.08.1",
          "minVersionCode": 0,
          "someFutureField": "ignored",
          "apks": {
            "arm64-v8a": { "url": "https://feralisme.fr/media/downloads/android/Feral-26.08.1-arm64-v8a.apk", "sha256": "aa", "versionCode": 4026081013, "size": 5 },
            "universal": { "url": "https://feralisme.fr/media/downloads/android/Feral-26.08.1.apk", "sha256": "bb", "versionCode": 4026081010 }
          }
        }
    """.trimIndent()

    private fun parse() = json.decodeFromString(UpdateManifest.serializer(), sampleJson)

    @Test
    fun `manifest parses and tolerates unknown fields`() {
        val manifest = parse()
        assertThat(manifest.versionName).isEqualTo("26.08.1")
        assertThat(manifest.apks).hasSize(2)
        assertThat(manifest.apks.getValue("arm64-v8a").versionCode).isEqualTo(4026081013L)
    }

    @Test
    fun `newer version for matching abi is offered`() {
        val update = parse().selectUpdate(
            supportedAbis = listOf("arm64-v8a", "armeabi-v7a"),
            currentVersionCode = 4025054003L,
            ignoredVersionCode = null,
        )
        assertThat(update).isNotNull()
        assertThat(update!!.versionCode).isEqualTo(4026081013L)
        assertThat(update.url).contains("arm64-v8a")
    }

    @Test
    fun `same or older version is not offered - anti downgrade`() {
        val manifest = parse()
        assertThat(manifest.selectUpdate(listOf("arm64-v8a"), 4026081013L, null)).isNull()
        assertThat(manifest.selectUpdate(listOf("arm64-v8a"), 5000000000L, null)).isNull()
    }

    @Test
    fun `ignored version is not offered again`() {
        val update = parse().selectUpdate(
            supportedAbis = listOf("arm64-v8a"),
            currentVersionCode = 1L,
            ignoredVersionCode = 4026081013L,
        )
        assertThat(update).isNull()
    }

    @Test
    fun `unknown abi falls back to universal apk`() {
        val update = parse().selectUpdate(
            supportedAbis = listOf("riscv64"),
            currentVersionCode = 1L,
            ignoredVersionCode = null,
        )
        assertThat(update).isNotNull()
        assertThat(update!!.versionCode).isEqualTo(4026081010L)
    }

    @Test
    fun `no matching apk yields no update`() {
        val manifest = json.decodeFromString(
            UpdateManifest.serializer(),
            """{ "versionName": "x", "apks": { "arm64-v8a": { "url": "u", "sha256": "s", "versionCode": 2 } } }""",
        )
        val update = manifest.selectUpdate(
            supportedAbis = listOf("riscv64"),
            currentVersionCode = 1L,
            ignoredVersionCode = null,
        )
        assertThat(update).isNull()
    }

    @Test
    fun `abi split of the same release is not offered to a universal install`() {
        // Installed universal 26.08.1 (…10) must not be offered the arm64 split (…13):
        // the last digit is the ABI code, not a newer release.
        assertThat(parse().selectUpdate(listOf("arm64-v8a"), 4026081010L, null)).isNull()
    }

    @Test
    fun `next release is offered to a universal install`() {
        assertThat(parse().selectUpdate(listOf("arm64-v8a"), 4026080000L, null)).isNotNull()
    }
}
