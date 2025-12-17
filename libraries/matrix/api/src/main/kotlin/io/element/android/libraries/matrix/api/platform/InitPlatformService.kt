/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api.platform

import io.element.android.libraries.matrix.api.tracing.TracingConfiguration

/**
 * This service is responsible for initializing the platform-related settings of the SDK.
 */
interface InitPlatformService {
    /**
     * Initialize the platform-related settings of the SDK.
     * @param tracingConfiguration the tracing configuration to use for logging.
     */
    fun init(tracingConfiguration: TracingConfiguration)
}
