/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.dateformatter.test

import io.element.android.libraries.dateformatter.api.DaySeparatorFormatter

class FakeDaySeparatorFormatter : DaySeparatorFormatter {
    private var format = ""

    fun givenFormat(format: String) {
        this.format = format
    }

    override fun format(timestamp: Long): String {
        return format
    }
}
