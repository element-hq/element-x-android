/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.matrix.impl.fixtures.fakes

import io.element.android.libraries.matrix.impl.fixtures.factories.anEventTimelineItemDebugInfo
import io.element.android.libraries.matrix.test.AN_EVENT_ID
import io.element.android.libraries.matrix.test.A_USER_ID
import org.matrix.rustcomponents.sdk.EventSendState
import org.matrix.rustcomponents.sdk.EventTimelineItem
import org.matrix.rustcomponents.sdk.EventTimelineItemDebugInfo
import org.matrix.rustcomponents.sdk.NoPointer
import org.matrix.rustcomponents.sdk.ProfileDetails
import org.matrix.rustcomponents.sdk.Reaction
import org.matrix.rustcomponents.sdk.Receipt
import org.matrix.rustcomponents.sdk.ShieldState
import org.matrix.rustcomponents.sdk.TimelineItemContent
import uniffi.matrix_sdk_ui.EventItemOrigin

class FakeRustEventTimelineItem(
    private val origin: EventItemOrigin? = null,
) : EventTimelineItem(NoPointer) {
    override fun origin(): EventItemOrigin? = origin
    override fun eventId(): String = AN_EVENT_ID.value
    override fun transactionId(): String? = null
    override fun isEditable(): Boolean = false
    override fun canBeRepliedTo(): Boolean = false
    override fun isLocal(): Boolean = false
    override fun isOwn(): Boolean = false
    override fun isRemote(): Boolean = false
    override fun localSendState(): EventSendState? = null
    override fun reactions(): List<Reaction> = emptyList()
    override fun readReceipts(): Map<String, Receipt> = emptyMap()
    override fun sender(): String = A_USER_ID.value
    override fun senderProfile(): ProfileDetails = ProfileDetails.Unavailable
    override fun timestamp(): ULong = 0u
    override fun content(): TimelineItemContent = FakeRustTimelineItemContent()
    override fun debugInfo(): EventTimelineItemDebugInfo = anEventTimelineItemDebugInfo()
    override fun getShield(strict: Boolean): ShieldState? = null
}
