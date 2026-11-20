/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.androidutils.toast

import android.content.Context
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.androidutils.system.toast
import io.element.android.libraries.di.annotations.ApplicationContext

@ContributesBinding(AppScope::class)
class AndroidToastHelper(
    @ApplicationContext private val context: Context,
) : ToastHelper {
    override fun show(resId: Int) {
        context.toast(resId)
    }
}
