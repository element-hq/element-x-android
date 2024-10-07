/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.libraries.push.impl.notifications

import io.element.android.libraries.core.meta.BuildMeta
import javax.inject.Inject

/**
 * Util class for creating notifications action Ids, using the application id.
 */
data class NotificationActionIds @Inject constructor(
    private val buildMeta: BuildMeta,
) {
    val join = "${buildMeta.applicationId}.NotificationActions.JOIN_ACTION"
    val reject = "${buildMeta.applicationId}.NotificationActions.REJECT_ACTION"
    val markRoomRead = "${buildMeta.applicationId}.NotificationActions.MARK_ROOM_READ_ACTION"
    val smartReply = "${buildMeta.applicationId}.NotificationActions.SMART_REPLY_ACTION"
    val dismissSummary = "${buildMeta.applicationId}.NotificationActions.DISMISS_SUMMARY_ACTION"
    val dismissRoom = "${buildMeta.applicationId}.NotificationActions.DISMISS_ROOM_NOTIF_ACTION"
    val dismissInvite = "${buildMeta.applicationId}.NotificationActions.DISMISS_INVITE_NOTIF_ACTION"
    val dismissEvent = "${buildMeta.applicationId}.NotificationActions.DISMISS_EVENT_NOTIF_ACTION"
    val diagnostic = "${buildMeta.applicationId}.NotificationActions.DIAGNOSTIC"
}
