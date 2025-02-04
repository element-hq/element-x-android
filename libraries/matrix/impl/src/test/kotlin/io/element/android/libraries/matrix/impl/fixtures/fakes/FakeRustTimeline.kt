/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.PaginationStatusListener
import org.matrix.rustcomponents.sdk.TaskHandle
import org.matrix.rustcomponents.sdk.Timeline
import org.matrix.rustcomponents.sdk.TimelineDiff
import org.matrix.rustcomponents.sdk.TimelineListener
import uniffi.matrix_sdk_ui.LiveBackPaginationStatus

class FakeRustTimeline : Timeline(NoPointer) {
    private var listener: TimelineListener? = null
    override suspend fun addListener(listener: TimelineListener): TaskHandle {
        this.listener = listener
        return FakeRustTaskHandle()
    }

    fun emitDiff(diff: List<TimelineDiff>) {
        listener!!.onUpdate(diff)
    }

    private var paginationStatusListener: PaginationStatusListener? = null
    override suspend fun subscribeToBackPaginationStatus(listener: PaginationStatusListener): TaskHandle {
        this.paginationStatusListener = listener
        return FakeRustTaskHandle()
    }

    fun emitPaginationStatus(status: LiveBackPaginationStatus) {
        paginationStatusListener!!.onUpdate(status)
    }

    override suspend fun fetchMembers() = Unit
}
