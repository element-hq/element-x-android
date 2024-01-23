/*
 * Copyright (c) 2023 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.element.android.features.ftue.impl.welcome.state

import android.content.SharedPreferences
import androidx.core.content.edit
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.DefaultPreferences
import io.element.android.libraries.di.SingleIn
import javax.inject.Inject

@ContributesBinding(AppScope::class)
@SingleIn(AppScope::class)
class AndroidWelcomeScreenState @Inject constructor(
    @DefaultPreferences private val sharedPreferences: SharedPreferences,
) : WelcomeScreenState {
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
