/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.services.toolbox.impl.sdk

import android.os.Build
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.services.toolbox.api.sdk.BuildVersionSdkIntProvider

@ContributesBinding(AppScope::class)
class DefaultBuildVersionSdkIntProvider :
    BuildVersionSdkIntProvider {
    override fun get() = Build.VERSION.SDK_INT
}
