/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.reportroom.impl

sealed interface ReportRoomEvent {
    data class UpdateReason(val reason: String) : ReportRoomEvent
    data object ToggleLeaveRoom : ReportRoomEvent
    data object Report : ReportRoomEvent
    data object ClearReportAction : ReportRoomEvent
}
