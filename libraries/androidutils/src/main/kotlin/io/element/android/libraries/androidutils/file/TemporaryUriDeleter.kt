/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.file

import android.content.Context
import android.net.Uri
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

interface TemporaryUriDeleter {
    /**
     * Delete the Uri only if it is a temporary one.
     */
    fun delete(uri: Uri?)
}

@ContributesBinding(AppScope::class)
class DefaultTemporaryUriDeleter @Inject constructor(
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
