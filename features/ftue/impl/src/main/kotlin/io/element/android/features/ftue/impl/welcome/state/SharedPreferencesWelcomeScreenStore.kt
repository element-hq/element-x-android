/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.ftue.impl.welcome.state

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.SingleIn
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class SharedPreferencesWelcomeScreenStore @Inject constructor(
    private val sharedPreferences: SharedPreferences,
) : WelcomeScreenStore {
    companion object {
        private const val IS_WELCOME_SCREEN_SHOWN = "is_welcome_screen_shown"
    }

    override fun isWelcomeScreenNeeded(): Boolean {
        return sharedPreferences.getBoolean(IS_WELCOME_SCREEN_SHOWN, false).not()
    }

    override fun setWelcomeScreenShown() {
        sharedPreferences.edit().putBoolean(IS_WELCOME_SCREEN_SHOWN, true).apply()
    }

    override fun reset() {
        sharedPreferences.edit {
            remove(IS_WELCOME_SCREEN_SHOWN)
        }
    }
}
