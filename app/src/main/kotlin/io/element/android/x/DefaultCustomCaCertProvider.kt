/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.x

import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import io.element.android.libraries.matrix.api.auth.CustomCaCertProvider
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultCustomCaCertProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : CustomCaCertProvider {
    override fun get(): List<ByteArray> {
        return listOf(
            context.resources.openRawResource(R.raw.element_io_ca).use { it.readBytes() }
        )
    }
}
