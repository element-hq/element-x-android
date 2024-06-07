/*
 * Copyright (c) 2022 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
