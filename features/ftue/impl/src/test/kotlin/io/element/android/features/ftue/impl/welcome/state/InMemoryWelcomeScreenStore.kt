/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.welcome.state

class InMemoryWelcomeScreenStore : WelcomeScreenStore {
    private var isWelcomeScreenNeeded = true

    override fun isWelcomeScreenNeeded(): Boolean {
        return isWelcomeScreenNeeded
    }

    override fun setWelcomeScreenShown() {
        isWelcomeScreenNeeded = false
    }

    override fun reset() {
        isWelcomeScreenNeeded = true
    }
}
