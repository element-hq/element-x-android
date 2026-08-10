/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils.fake

import android.content.ContentProvider
import android.content.ContentValues
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.test.core.app.ApplicationProvider
import org.robolectric.shadows.ShadowContentResolver
import java.io.File
import java.nio.file.Files

/**
 * Stands in for the MediaStore provider: it records the display names an insert is attempted with,
 * writes what is copied into it to a temporary directory, and can make any display name collide the
 * way MediaStore does once a directory already holds 32 files with the same name.
 */
class FakeMediaStoreContentProvider : ContentProvider() {
    private val directory: File = Files.createTempDirectory("fake-media-store").toFile()

    val insertedDisplayNames = mutableListOf<String>()

    var conflictingDisplayNames: Set<String> = emptySet()

    /** When true, an insert reports that it created nothing, the way a rejected insert does. */
    var returnsNoUriOnInsert: Boolean = false

    fun savedFileNames(): List<String> = directory.list().orEmpty().toList()

    fun savedContent(displayName: String): String = File(directory, displayName).readText()

    override fun onCreate() = true

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val displayName = values?.getAsString(MediaStore.MediaColumns.DISPLAY_NAME).orEmpty()
        insertedDisplayNames += displayName
        check(displayName !in conflictingDisplayNames) { "Failed to build unique file" }
        return if (returnsNoUriOnInsert) null else uri.buildUpon().appendPath(displayName).build()
    }

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor {
        val file = File(directory, uri.lastPathSegment.orEmpty())
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_CREATE or ParcelFileDescriptor.MODE_READ_WRITE)
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? = null

    override fun getType(uri: Uri): String? = null

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = 0

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = 0
}

fun registerFakeMediaStoreContentProvider(): FakeMediaStoreContentProvider {
    return FakeMediaStoreContentProvider().apply {
        attachInfo(
            ApplicationProvider.getApplicationContext(),
            ProviderInfo().apply { authority = MediaStore.AUTHORITY }
        )
        ShadowContentResolver.registerProviderInternal(MediaStore.AUTHORITY, this)
    }
}
