/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.onboarding

import android.annotation.SuppressLint
import android.content.Context
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.di.ApplicationContext
import javax.inject.Inject

fun interface OnBoardingLogoResIdProvider {
    fun get(): Int?
}

@ContributesBinding(AppScope::class)
class DefaultOnBoardingLogoResIdProvider @Inject constructor(
    @ApplicationContext private val context: Context,
) : OnBoardingLogoResIdProvider {
    @SuppressLint("DiscouragedApi")
    override fun get(): Int? {
        val resId = context.resources
            .getIdentifier("onboarding_logo", "drawable", context.packageName)
            .takeIf { it != 0 }
        return resId
    }
}
