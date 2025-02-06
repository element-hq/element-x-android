/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.lockscreen.impl.unlock.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.enterprise.api.EnterpriseService
import io.element.android.features.lockscreen.api.LockScreenLockState
import io.element.android.features.lockscreen.api.LockScreenService
import io.element.android.features.lockscreen.impl.unlock.PinUnlockPresenter
import io.element.android.features.lockscreen.impl.unlock.PinUnlockView
import io.element.android.features.lockscreen.impl.unlock.di.PinUnlockBindings
import io.element.android.features.logout.api.util.onSuccessLogout
import io.element.android.libraries.architecture.bindings
import io.element.android.libraries.designsystem.theme.ElementThemeApp
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
    @Inject lateinit var enterpriseService: EnterpriseService

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        bindings<PinUnlockBindings>().inject(this)
        setContent {
            ElementThemeApp(
                appPreferencesStore = appPreferencesStore,
                enterpriseService = enterpriseService,
            ) {
                val state = presenter.present()
                val isDark = ElementTheme.isLightTheme.not()
                PinUnlockView(
                    state = state,
                    isInAppUnlock = false,
                    onSuccessLogout = { onSuccessLogout(this, isDark, it) },
                )
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
