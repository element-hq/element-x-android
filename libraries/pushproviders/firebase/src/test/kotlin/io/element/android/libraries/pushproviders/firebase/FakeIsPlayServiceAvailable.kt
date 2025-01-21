/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase

class FakeIsPlayServiceAvailable(
    private val isAvailable: Boolean,
) : IsPlayServiceAvailable {
    override fun isAvailable() = isAvailable
}
