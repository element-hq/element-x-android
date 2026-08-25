/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.share.test

import io.element.android.features.share.api.DirectShareShortcutsObserver

class FakeDirectShareShortcutsObserver(
    private val startLambda: () -> Unit = {},
) : DirectShareShortcutsObserver {
    override fun start() {
        startLambda()
    }
}
