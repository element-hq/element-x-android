/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.google.common.truth.Truth.assertThat
import io.element.android.features.share.api.ShareIntentData
import io.element.android.features.share.api.UriToShare
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.robolectric.RuntimeEnvironment

class DefaultShareIntentHandlerTest : RobolectricTest() {
    @Test
    fun `an image is shared as an uri`() {
        val uri = "content://sender/image.jpg".toUri()
        val result = createDefaultShareIntentHandler().handleIncomingShareIntent(
            aSendIntent(type = "image/jpeg", uri = uri)
        )
        assertThat(result).isEqualTo(
            ShareIntentData.Uris(
                text = null,
                uris = listOf(UriToShare(uri = uri, mimeType = "image/jpeg")),
            )
        )
    }

    @Test
    fun `an uri with an unlisted mime type is shared`() {
        val uri = "content://sender/logs.zip".toUri()
        val result = createDefaultShareIntentHandler().handleIncomingShareIntent(
            aSendIntent(type = "message/rfc822", uri = uri)
        )
        assertThat(result).isEqualTo(
            ShareIntentData.Uris(
                text = null,
                uris = listOf(UriToShare(uri = uri, mimeType = "message/rfc822")),
            )
        )
    }

    @Test
    fun `an uri with an unlisted mime type keeps the text as a caption`() {
        val uri = "content://sender/logs.zip".toUri()
        val result = createDefaultShareIntentHandler().handleIncomingShareIntent(
            aSendIntent(type = "message/rfc822", uri = uri).apply {
                putExtra(Intent.EXTRA_TEXT, "a caption")
            }
        )
        assertThat(result).isEqualTo(
            ShareIntentData.Uris(
                text = "a caption",
                uris = listOf(UriToShare(uri = uri, mimeType = "message/rfc822")),
            )
        )
    }

    @Test
    fun `several uris with an unlisted mime type are shared`() {
        val firstUri = "content://sender/first.zip".toUri()
        val secondUri = "content://sender/second.zip".toUri()
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "message/rfc822"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(firstUri, secondUri))
        }
        val result = createDefaultShareIntentHandler().handleIncomingShareIntent(intent)
        assertThat(result).isEqualTo(
            ShareIntentData.Uris(
                text = null,
                uris = listOf(
                    UriToShare(uri = firstUri, mimeType = "message/rfc822"),
                    UriToShare(uri = secondUri, mimeType = "message/rfc822"),
                ),
            )
        )
    }

    @Test
    fun `a plain text is shared as a text`() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "a text")
        }
        val result = createDefaultShareIntentHandler().handleIncomingShareIntent(intent)
        assertThat(result).isEqualTo(ShareIntentData.PlainText("a text"))
    }

    @Test
    fun `an intent with neither uri nor text is not handled`() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
        }
        val result = createDefaultShareIntentHandler().handleIncomingShareIntent(intent)
        assertThat(result).isNull()
    }

    private fun aSendIntent(type: String, uri: Uri) = Intent(Intent.ACTION_SEND).apply {
        this.type = type
        putExtra(Intent.EXTRA_STREAM, uri)
    }

    private fun createDefaultShareIntentHandler() = DefaultShareIntentHandler(
        context = RuntimeEnvironment.getApplication(),
    )
}
