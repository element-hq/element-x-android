/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import android.content.Context
import android.media.RingtoneManager
import androidx.core.net.toUri
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.annotations.ApplicationContext
import io.element.android.libraries.push.api.notifications.SoundDisplayNameResolver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ContributesBinding(AppScope::class)
class DefaultSoundDisplayNameResolver(
    @ApplicationContext private val context: Context,
) : SoundDisplayNameResolver {
    override suspend fun resolveCustomSoundTitle(uri: String): String? = withContext(Dispatchers.IO) {
        runCatchingExceptions {
            RingtoneManager.getRingtone(context, uri.toUri())?.getTitle(context)
        }.getOrNull()
    }
}
