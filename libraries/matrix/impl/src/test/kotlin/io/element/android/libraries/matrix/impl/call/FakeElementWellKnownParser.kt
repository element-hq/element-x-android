/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.call

import org.matrix.rustcomponents.sdk.ElementWellKnown

class FakeElementWellKnownParser(
    private val result: Result<ElementWellKnown>
) : ElementWellKnownParser {
    override fun parse(str: String): Result<ElementWellKnown> {
        return result
    }
}
