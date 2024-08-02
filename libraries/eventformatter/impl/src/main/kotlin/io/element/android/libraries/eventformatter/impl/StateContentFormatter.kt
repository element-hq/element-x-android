/*
 * Copyright (c) 2023 New Vector Ltd
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

package io.element.android.libraries.eventformatter.impl

import io.element.android.libraries.eventformatter.impl.mode.RenderingMode
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.ui.strings.CommonStrings
import io.element.android.services.toolbox.api.strings.StringProvider
import timber.log.Timber
import javax.inject.Inject

class StateContentFormatter @Inject constructor(
    private val sp: StringProvider,
) {
    fun format(
        stateContent: StateContent,
        senderDisambiguatedDisplayName: String,
        senderIsYou: Boolean,
        renderingMode: RenderingMode,
    ): CharSequence? {
        return when (val content = stateContent.content) {
            is OtherState.RoomAvatar -> {
                val hasAvatarUrl = content.url != null
                when {
                    senderIsYou && hasAvatarUrl -> sp.getString(R.string.state_event_room_avatar_changed_by_you)
                    senderIsYou && !hasAvatarUrl -> sp.getString(R.string.state_event_room_avatar_removed_by_you)
                    !senderIsYou && hasAvatarUrl -> sp.getString(R.string.state_event_room_avatar_changed, senderDisambiguatedDisplayName)
                    else -> sp.getString(R.string.state_event_room_avatar_removed, senderDisambiguatedDisplayName)
                }
            }
            is OtherState.RoomCreate -> {
                if (senderIsYou) {
                    sp.getString(R.string.state_event_room_created_by_you)
                } else {
                    sp.getString(R.string.state_event_room_created, senderDisambiguatedDisplayName)
                }
            }
            is OtherState.RoomEncryption -> sp.getString(CommonStrings.common_encryption_enabled)
            is OtherState.RoomName -> {
                val hasRoomName = content.name != null
                when {
                    senderIsYou && hasRoomName -> sp.getString(R.string.state_event_room_name_changed_by_you, content.name)
                    senderIsYou && !hasRoomName -> sp.getString(R.string.state_event_room_name_removed_by_you)
                    !senderIsYou && hasRoomName -> sp.getString(R.string.state_event_room_name_changed, senderDisambiguatedDisplayName, content.name)
                    else -> sp.getString(R.string.state_event_room_name_removed, senderDisambiguatedDisplayName)
                }
            }
            is OtherState.RoomThirdPartyInvite -> {
                if (content.displayName == null) {
                    Timber.e("RoomThirdPartyInvite undisplayable due to missing name")
                    return null
                }
                if (senderIsYou) {
                    sp.getString(R.string.state_event_room_third_party_invite_by_you, content.displayName)
                } else {
                    sp.getString(R.string.state_event_room_third_party_invite, senderDisambiguatedDisplayName, content.displayName)
                }
            }
            is OtherState.RoomTopic -> {
                val hasRoomTopic = content.topic != null
                when {
                    senderIsYou && hasRoomTopic -> sp.getString(R.string.state_event_room_topic_changed_by_you, content.topic)
                    senderIsYou && !hasRoomTopic -> sp.getString(R.string.state_event_room_topic_removed_by_you)
                    !senderIsYou && hasRoomTopic -> sp.getString(R.string.state_event_room_topic_changed, senderDisambiguatedDisplayName, content.topic)
                    else -> sp.getString(R.string.state_event_room_topic_removed, senderDisambiguatedDisplayName)
                }
            }
            OtherState.RoomPinnedEvents -> {
                when {
                    //TODO manage all cases when available
                    senderIsYou -> sp.getString(R.string.state_event_room_pinned_events_changed_by_you)
                    else -> sp.getString(R.string.state_event_room_pinned_events_changed, senderDisambiguatedDisplayName)
                }
            }
            is OtherState.Custom -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "Custom event ${content.eventType}"
                }
            }
            OtherState.PolicyRuleRoom -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "PolicyRuleRoom"
                }
            }
            OtherState.PolicyRuleServer -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "PolicyRuleServer"
                }
            }
            OtherState.PolicyRuleUser -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "PolicyRuleUser"
                }
            }
            OtherState.RoomAliases -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "RoomAliases"
                }
            }
            OtherState.RoomCanonicalAlias -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "RoomCanonicalAlias"
                }
            }
            OtherState.RoomGuestAccess -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "RoomGuestAccess"
                }
            }
            OtherState.RoomHistoryVisibility -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "RoomHistoryVisibility"
                }
            }
            OtherState.RoomJoinRules -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "RoomJoinRules"
                }
            }
            is OtherState.RoomUserPowerLevels -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "RoomPowerLevels"
                }
            }
            OtherState.RoomServerAcl -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "RoomServerAcl"
                }
            }
            OtherState.RoomTombstone -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "RoomTombstone"
                }
            }
            OtherState.SpaceChild -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "SpaceChild"
                }
            }
            OtherState.SpaceParent -> when (renderingMode) {
                RenderingMode.RoomList -> {
                    Timber.v("Filtering timeline item for room state change: $content")
                    null
                }
                RenderingMode.Timeline -> {
                    "SpaceParent"
                }
            }
        }
    }
}
