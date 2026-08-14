/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.wellknown.impl

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.json.JsonProvider
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.wellknown.api.ElementWellKnown
import io.element.android.libraries.wellknown.api.ElementWellKnownParser

@ContributesBinding(AppScope::class)
class DefaultElementWellKnownParser(
    private val jsonProvider: JsonProvider,
) : ElementWellKnownParser {
    override fun parse(json: String): Result<ElementWellKnown> {
        return runCatchingExceptions {
            val decoder = jsonProvider()
            decoder.decodeFromString<InternalElementWellKnown>(json).map()
        }
    }
}
