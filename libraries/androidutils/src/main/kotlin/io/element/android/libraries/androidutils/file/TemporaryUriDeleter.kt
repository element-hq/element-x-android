/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.file

import android.content.Context
import android.net.Uri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.di.annotations.ApplicationContext
import timber.log.Timber

interface TemporaryUriDeleter {
    /**
     * Delete the Uri only if it is a temporary one.
     */
    fun delete(uri: Uri?)
}

@ContributesBinding(AppScope::class)
class DefaultTemporaryUriDeleter(
    @ApplicationContext private val context: Context,
) : TemporaryUriDeleter {
    private val baseCacheUri = "content://${context.packageName}.fileprovider/cache"

    override fun delete(uri: Uri?) {
        uri ?: return
        if (uri.toString().startsWith(baseCacheUri)) {
            context.contentResolver.delete(uri, null, null)
        } else {
            Timber.d("Do not delete the uri")
        }
    }
}
