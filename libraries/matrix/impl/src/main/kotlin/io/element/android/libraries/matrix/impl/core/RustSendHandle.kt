/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2024, 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.core

import io.element.android.libraries.core.extensions.runCatchingExceptions
import io.element.android.libraries.matrix.api.core.SendHandle

class RustSendHandle(
    val inner: org.matrix.rustcomponents.sdk.SendHandle,
) : SendHandle {
    override suspend fun retry(): Result<Unit> {
        return runCatchingExceptions {
            inner.tryResend()
        }
    }
}
