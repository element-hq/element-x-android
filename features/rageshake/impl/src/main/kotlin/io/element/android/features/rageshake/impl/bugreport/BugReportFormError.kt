/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.rageshake.impl.bugreport

sealed class BugReportFormError : Exception() {
    data object DescriptionTooShort : BugReportFormError()
}
