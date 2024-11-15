/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.core

import io.element.android.libraries.matrix.api.core.SendHandle

class RustSendHandle(
    val inner: org.matrix.rustcomponents.sdk.SendHandle,
) : SendHandle {
    override suspend fun retry(): Result<Unit> {
        return runCatching {
            inner.tryResend()
        }
    }
}
