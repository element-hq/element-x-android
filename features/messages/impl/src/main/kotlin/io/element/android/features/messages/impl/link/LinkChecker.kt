/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.link

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.core.data.tryOrNull
import io.element.android.libraries.core.extensions.containsRtLOverride
import io.element.android.wysiwyg.link.Link
import java.net.URI

interface LinkChecker {
    fun isSafe(link: Link): Boolean
}

@ContributesBinding(AppScope::class)
class DefaultLinkChecker : LinkChecker {
    override fun isSafe(link: Link): Boolean {
        return if (link.url.containsRtLOverride()) {
            false
        } else {
            val textUrl = tryOrNull { URI(link.text).toURL() }
            val urlUrl = tryOrNull { URI(link.url).toURL() }
            if (textUrl == null || urlUrl == null) {
                // The text is not a Url, or the url is not valid
                true
            } else {
                // the hosts must match
                textUrl.host == urlUrl.host
            }
        }
    }
}
