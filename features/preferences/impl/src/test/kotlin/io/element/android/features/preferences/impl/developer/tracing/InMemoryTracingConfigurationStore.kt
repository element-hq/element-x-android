/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.preferences.impl.developer.tracing

import io.element.android.libraries.matrix.api.tracing.LogLevel
import io.element.android.libraries.matrix.api.tracing.Target

class InMemoryTracingConfigurationStore : TracingConfigurationStore {
    var hasResetBeenCalled = false
        private set
    var hasStoreLogLevelBeenCalled = false
        private set
    private var logLevel: LogLevel? = null

    fun givenLogLevel(logLevel: LogLevel?) {
        this.logLevel = logLevel
    }

    override fun getLogLevel(target: Target): LogLevel? {
        return logLevel
    }

    override fun storeLogLevel(target: Target, logLevel: LogLevel) {
        hasStoreLogLevelBeenCalled = true
    }

    override fun reset() {
        hasResetBeenCalled = true
    }
}
