/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.core.coroutine

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

/**
 * Returns the first element of the flow that is an instance of [T], waiting for it if necessary.
 */
suspend inline fun <reified T> Flow<*>.firstInstanceOf(): T {
    return first { it is T } as T
}
