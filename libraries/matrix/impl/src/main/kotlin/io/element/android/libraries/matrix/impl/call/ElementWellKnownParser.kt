/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.call

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.di.AppScope
import org.matrix.rustcomponents.sdk.ElementWellKnown
import org.matrix.rustcomponents.sdk.makeElementWellKnown
import javax.inject.Inject

interface ElementWellKnownParser {
    fun parse(str: String): Result<ElementWellKnown>
}

@ContributesBinding(AppScope::class)
class RustElementWellKnownParser @Inject constructor() : ElementWellKnownParser {
    override fun parse(str: String): Result<ElementWellKnown> {
        return runCatchingExceptions {
            makeElementWellKnown(str)
        }
    }
}
