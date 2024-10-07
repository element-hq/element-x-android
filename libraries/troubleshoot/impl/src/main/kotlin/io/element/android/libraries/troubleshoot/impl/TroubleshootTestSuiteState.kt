/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.troubleshoot.impl

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.libraries.troubleshoot.api.test.NotificationTroubleshootTestState
import kotlinx.collections.immutable.ImmutableList

data class TroubleshootTestSuiteState(
    val mainState: AsyncAction<Unit>,
    val tests: ImmutableList<NotificationTroubleshootTestState>,
)
