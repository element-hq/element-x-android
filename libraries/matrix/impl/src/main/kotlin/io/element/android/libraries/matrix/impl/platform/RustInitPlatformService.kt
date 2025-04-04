/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.platform

import com.squareup.anvil.annotations.ContributesBinding
import io.element.android.libraries.di.AppScope
import io.element.android.libraries.matrix.api.platform.InitPlatformService
import io.element.android.libraries.matrix.api.tracing.TracingConfiguration
import io.element.android.libraries.matrix.impl.tracing.map
import org.matrix.rustcomponents.sdk.initPlatform
import javax.inject.Inject

@ContributesBinding(AppScope::class)
class RustInitPlatformService @Inject constructor() : InitPlatformService {
    override fun init(tracingConfiguration: TracingConfiguration) {
        initPlatform(
            config = tracingConfiguration.map(),
            useLightweightTokioRuntime = false
        )
    }
}
