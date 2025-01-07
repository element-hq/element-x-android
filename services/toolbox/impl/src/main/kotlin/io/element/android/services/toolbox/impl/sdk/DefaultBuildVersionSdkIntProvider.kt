/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.impl.sdk

import android.os.Build
import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class DefaultBuildVersionSdkIntProvider @Inject constructor() :
    BuildVersionSdkIntProvider {
    override fun get() = Build.VERSION.SDK_INT
}
