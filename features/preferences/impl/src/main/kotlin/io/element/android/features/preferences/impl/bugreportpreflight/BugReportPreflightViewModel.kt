/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.preferences.impl.bugreportpreflight

import dev.zacsweers.metro.Inject
import io.element.android.features.preferences.impl.diagnostics.BugReportFormatter
import io.element.android.features.preferences.impl.diagnostics.DiagnosticsRepository
import io.element.android.features.preferences.impl.diagnostics.PrivacyRedactor
import io.element.android.libraries.di.annotations.SessionCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
@Inject
class BugReportPreflightViewModel(
    diagnosticsRepository: DiagnosticsRepository,
    bugReportFormatter: BugReportFormatter,
    privacyRedactor: PrivacyRedactor,
    @SessionCoroutineScope sessionCoroutineScope: CoroutineScope,
) {
    val uiState: StateFlow<BugReportPreflightUiState> = diagnosticsRepository
        .getDiagnostics()
        .map { diagnostics ->
            val report = bugReportFormatter.format(diagnostics)
            val redacted = privacyRedactor.redact(report)
            BugReportPreflightUiState.Content(redacted)
        }
        .stateIn(
            scope = sessionCoroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = BugReportPreflightUiState.Loading,
        )
}
