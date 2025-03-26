/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.link

import io.element.android.tests.testutils.lambda.lambdaError
import io.element.android.wysiwyg.link.Link

class FakeLinkChecker(
    private val isSafeResult: (Link) -> Boolean = { lambdaError() }
) : LinkChecker {
    override fun isSafe(link: Link) = isSafeResult(link)
}
