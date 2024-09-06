/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.samples.minimal

import android.net.Uri
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser

class OnlyFallbackPermalinkParser : PermalinkParser {
    override fun parse(uriString: String): PermalinkData {
        return PermalinkData.FallbackLink(Uri.parse(uriString))
    }
}
