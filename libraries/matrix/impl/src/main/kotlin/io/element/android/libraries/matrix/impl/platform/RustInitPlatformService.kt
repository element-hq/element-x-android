/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.platform

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import io.element.android.libraries.matrix.api.platform.InitPlatformService
import io.element.android.libraries.matrix.api.tracing.TracingConfiguration
import io.element.android.libraries.matrix.impl.tracing.map
import org.matrix.rustcomponents.sdk.initPlatform

@ContributesBinding(AppScope::class)
class RustInitPlatformService : InitPlatformService {
    override fun init(tracingConfiguration: TracingConfiguration) {
        initPlatform(
            config = tracingConfiguration.map(),
            useLightweightTokioRuntime = false
        )
    }
}
