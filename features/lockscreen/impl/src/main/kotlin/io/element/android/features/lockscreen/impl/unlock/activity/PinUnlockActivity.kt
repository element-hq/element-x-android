/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.features.lockscreen.impl.unlock.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import io.element.android.compound.theme.ElementTheme
import io.element.android.compound.theme.Theme
import io.element.android.compound.theme.isDark
import io.element.android.compound.theme.mapToTheme
import io.element.android.features.lockscreen.api.LockScreenLockState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.lockscreen.impl.unlock.PinUnlockPresenter
import io.element.android.features.lockscreen.impl.unlock.PinUnlockView
import io.element.android.features.lockscreen.impl.unlock.di.PinUnlockBindings
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.preferences.api.store.AppPreferencesStore
import kotlinx.coroutines.launch
import javax.inject.Inject

class PinUnlockActivity : AppCompatActivity() {
    internal companion object {
        fun newIntent(context: Context): Intent {
            return Intent(context, PinUnlockActivity::class.java)
        }
    }

    @Inject lateinit var presenter: PinUnlockPresenter
    @Inject lateinit var lockScreenService: LockScreenService
    @Inject lateinit var appPreferencesStore: AppPreferencesStore

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        bindings<PinUnlockBindings>().inject(this)
        setContent {
            val theme by remember {
                appPreferencesStore.getThemeFlow().mapToTheme()
            }
                .collectAsState(initial = Theme.System)
            ElementTheme(
                darkTheme = theme.isDark()
            ) {
                val state = presenter.present()
                PinUnlockView(state = state, isInAppUnlock = false)
            }
        }
        lifecycleScope.launch {
            lockScreenService.lockState.collect { state ->
                if (state == LockScreenLockState.Unlocked) {
                    finish()
                }
            }
        }
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                moveTaskToBack(true)
            }
        }
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }
}
