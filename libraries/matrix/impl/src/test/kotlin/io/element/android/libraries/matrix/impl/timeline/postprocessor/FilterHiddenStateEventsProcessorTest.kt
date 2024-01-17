/*
 * Copyright (c) 2024 New Vector Ltd
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

package io.element.android.libraries.matrix.impl.timeline.postprocessor

import com.google.common.truth.Truth.assertThat
import io.element.android.libraries.matrix.api.timeline.MatrixTimelineItem
import io.element.android.libraries.matrix.api.timeline.item.event.OtherState
import io.element.android.libraries.matrix.api.timeline.item.event.StateContent
import io.element.android.libraries.matrix.api.timeline.item.virtual.VirtualTimelineItem
import io.element.android.libraries.matrix.test.timeline.anEventTimelineItem
import org.junit.Test

class FilterHiddenStateEventsProcessorTest {
    @Test
    fun test() {
        val items = listOf(
            // These are visible because they're not state events
            MatrixTimelineItem.Other,
            MatrixTimelineItem.Virtual("virtual", VirtualTimelineItem.ReadMarker),
            MatrixTimelineItem.Event("event", anEventTimelineItem()),
            // These are visible state events
            MatrixTimelineItem.Event("m.room.avatar", anEventTimelineItem(content = StateContent("", OtherState.RoomAvatar("")))),
            MatrixTimelineItem.Event("m.room.create", anEventTimelineItem(content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event("m.room.encrypted", anEventTimelineItem(content = StateContent("", OtherState.RoomEncryption))),
            MatrixTimelineItem.Event("m.room.name", anEventTimelineItem(content = StateContent("", OtherState.RoomName("")))),
            MatrixTimelineItem.Event("m.room.third_party_invite", anEventTimelineItem(content = StateContent("", OtherState.RoomThirdPartyInvite("")))),
            MatrixTimelineItem.Event("m.room.topic", anEventTimelineItem(content = StateContent("", OtherState.RoomTopic("")))),
            MatrixTimelineItem.Event("m.room.custom", anEventTimelineItem(content = StateContent("", OtherState.Custom("")))),
            // These ones are hidden
            MatrixTimelineItem.Event("m.room.aliases", anEventTimelineItem(content = StateContent("", OtherState.RoomAliases))),
            MatrixTimelineItem.Event("m.room.canonical_alias", anEventTimelineItem(content = StateContent("", OtherState.RoomCanonicalAlias))),
            MatrixTimelineItem.Event("m.room.guest_access", anEventTimelineItem(content = StateContent("", OtherState.RoomGuestAccess))),
            MatrixTimelineItem.Event("m.room.history_visibility", anEventTimelineItem(content = StateContent("", OtherState.RoomHistoryVisibility))),
            MatrixTimelineItem.Event("m.room.join_rules", anEventTimelineItem(content = StateContent("", OtherState.RoomJoinRules))),
            MatrixTimelineItem.Event("m.room.pinned_events", anEventTimelineItem(content = StateContent("", OtherState.RoomPinnedEvents))),
            MatrixTimelineItem.Event("m.room.power_levels", anEventTimelineItem(content = StateContent("", OtherState.RoomPowerLevels))),
            MatrixTimelineItem.Event("m.room.server_acl", anEventTimelineItem(content = StateContent("", OtherState.RoomServerAcl))),
            MatrixTimelineItem.Event("m.room.tombstone", anEventTimelineItem(content = StateContent("", OtherState.RoomTombstone))),
            MatrixTimelineItem.Event("m.space.child", anEventTimelineItem(content = StateContent("", OtherState.SpaceChild))),
            MatrixTimelineItem.Event("m.space.parent", anEventTimelineItem(content = StateContent("", OtherState.SpaceParent))),
            MatrixTimelineItem.Event("m.room.policy.rule.room", anEventTimelineItem(content = StateContent("", OtherState.PolicyRuleRoom))),
            MatrixTimelineItem.Event("m.room.policy.rule.server", anEventTimelineItem(content = StateContent("", OtherState.PolicyRuleServer))),
            MatrixTimelineItem.Event("m.room.policy.rule.user", anEventTimelineItem(content = StateContent("", OtherState.PolicyRuleUser))),
        )

        val expected = listOf(
            MatrixTimelineItem.Other,
            MatrixTimelineItem.Virtual("virtual", VirtualTimelineItem.ReadMarker),
            MatrixTimelineItem.Event("event", anEventTimelineItem()),
            MatrixTimelineItem.Event("m.room.avatar", anEventTimelineItem(content = StateContent("", OtherState.RoomAvatar("")))),
            MatrixTimelineItem.Event("m.room.create", anEventTimelineItem(content = StateContent("", OtherState.RoomCreate))),
            MatrixTimelineItem.Event("m.room.encrypted", anEventTimelineItem(content = StateContent("", OtherState.RoomEncryption))),
            MatrixTimelineItem.Event("m.room.name", anEventTimelineItem(content = StateContent("", OtherState.RoomName("")))),
            MatrixTimelineItem.Event("m.room.third_party_invite", anEventTimelineItem(content = StateContent("", OtherState.RoomThirdPartyInvite("")))),
            MatrixTimelineItem.Event("m.room.topic", anEventTimelineItem(content = StateContent("", OtherState.RoomTopic("")))),
            MatrixTimelineItem.Event("m.room.custom", anEventTimelineItem(content = StateContent("", OtherState.Custom("")))),
        )

        val processor = FilterHiddenStateEventsProcessor()

        assertThat(processor.process(items)).isEqualTo(expected)
    }
}
