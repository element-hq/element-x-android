/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.impl

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.pm.ShortcutManagerCompat
import com.google.common.truth.Truth.assertThat
import io.element.android.features.share.api.ShareIntentData
import io.element.android.libraries.core.mimetype.MimeTypes
import io.element.android.libraries.matrix.test.A_ROOM_ID
import io.element.android.libraries.matrix.test.A_SESSION_ID
import io.element.android.tests.testutils.robolectric.RobolectricTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

class DefaultShareIntentHandlerTest: RobolectricTest() {
    private val context: Context = RuntimeEnvironment.getApplication()
    private val handler = DefaultShareIntentHandler(context)

    @Test
    fun `handleIncomingShareIntent returns PlainText without target shortcut when extra is missing`() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MimeTypes.PlainText
            putExtra(Intent.EXTRA_TEXT, "Hello world")
        }

        val result = handler.handleIncomingShareIntent(intent)
        assertThat(result).isInstanceOf(ShareIntentData.PlainText::class.java)
        val plainText = result as ShareIntentData.PlainText
        assertThat(plainText.content).isEqualTo("Hello world")
        assertThat(plainText.targetSessionId).isNull()
        assertThat(plainText.targetRoomId).isNull()
    }

    @Test
    fun `handleIncomingShareIntent returns PlainText with target shortcut when EXTRA_SHORTCUT_ID is present`() {
        val shortcutId = "${A_SESSION_ID.value}-${A_ROOM_ID.value}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MimeTypes.PlainText
            putExtra(Intent.EXTRA_TEXT, "Hello world")
            putExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID, shortcutId)
        }

        val result = handler.handleIncomingShareIntent(intent)
        assertThat(result).isInstanceOf(ShareIntentData.PlainText::class.java)
        val plainText = result as ShareIntentData.PlainText
        assertThat(plainText.content).isEqualTo("Hello world")
        assertThat(plainText.targetSessionId).isEqualTo(A_SESSION_ID)
        assertThat(plainText.targetRoomId).isEqualTo(A_ROOM_ID)
    }

    @Test
    fun `handleIncomingShareIntent returns Uris with target shortcut when EXTRA_SHORTCUT_ID is present`() {
        val shortcutId = "${A_SESSION_ID.value}-${A_ROOM_ID.value}"
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MimeTypes.Jpeg
            putExtra(Intent.EXTRA_STREAM, Uri.parse("content://media/image.jpg"))
            putExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID, shortcutId)
        }

        val result = handler.handleIncomingShareIntent(intent)
        assertThat(result).isInstanceOf(ShareIntentData.Uris::class.java)
        val urisData = result as ShareIntentData.Uris
        assertThat(urisData.targetSessionId).isEqualTo(A_SESSION_ID)
        assertThat(urisData.targetRoomId).isEqualTo(A_ROOM_ID)
    }

    @Test
    fun `handleIncomingShareIntent ignores malformed shortcut ID`() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = MimeTypes.PlainText
            putExtra(Intent.EXTRA_TEXT, "Hello world")
            putExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID, "invalid_shortcut_without_dash")
        }

        val result = handler.handleIncomingShareIntent(intent)
        assertThat(result).isInstanceOf(ShareIntentData.PlainText::class.java)
        val plainText = result as ShareIntentData.PlainText
        assertThat(plainText.targetSessionId).isNull()
        assertThat(plainText.targetRoomId).isNull()
    }
}
