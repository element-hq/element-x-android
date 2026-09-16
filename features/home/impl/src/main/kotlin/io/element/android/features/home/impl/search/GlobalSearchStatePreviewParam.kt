/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.home.impl.search

import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.features.home.impl.roomlist.aRoomListRoomSummaryList
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomAlias
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.room.CallIntentConsensus
import io.element.android.libraries.matrix.api.room.CurrentUserMembership
import io.element.android.libraries.matrix.api.room.RoomInfo
import io.element.android.libraries.matrix.api.room.RoomMember
import io.element.android.libraries.matrix.api.room.RoomNotificationMode
import io.element.android.libraries.matrix.api.room.history.RoomHistoryVisibility
import io.element.android.libraries.matrix.api.room.join.JoinRule
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevels
import io.element.android.libraries.matrix.api.room.powerlevels.RoomPowerLevelsValues
import io.element.android.libraries.matrix.api.room.tombstone.SuccessorRoom
import io.element.android.libraries.matrix.api.search.MessageSearchResult
import io.element.android.libraries.matrix.api.timeline.item.EventThreadInfo
import io.element.android.libraries.matrix.api.timeline.item.event.InReplyTo
import io.element.android.libraries.matrix.api.timeline.item.event.MessageContent
import io.element.android.libraries.matrix.api.timeline.item.event.MessageType
import io.element.android.libraries.matrix.api.timeline.item.event.ProfileDetails
import io.element.android.libraries.matrix.api.timeline.item.event.TextMessageType
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.libraries.matrix.ui.components.AttachmentThumbnailType
import io.element.android.libraries.matrix.ui.messages.reply.aProfileDetailsReady
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList

class GlobalSearchStatePreviewParam : PreviewParameterProvider<GlobalSearchState> {
    override val values: Sequence<GlobalSearchState>
        get() = sequenceOf(
            // Not enabled, no preview
            aGlobalSearchState(isEnabled = false),
            aGlobalSearchState(isSearchActive = true, queryState = TextFieldState("Query")),
            aGlobalSearchState(
                isSearchActive = true,
                currentTarget = GlobalSearchTarget.ROOMS,
                queryState = TextFieldState("Query"),
                results = AsyncData.Loading(),
            ),
            aGlobalSearchState(
                isSearchActive = true,
                currentTarget = GlobalSearchTarget.ROOMS,
                queryState = TextFieldState("Query"),
                results = AsyncData.Success(
                    GlobalSearchResults.RoomListResults(
                        results = aRoomListRoomSummaryList().mapIndexed { index, summary ->
                            summary.copy(name = "Room with Query #${index + 1}")
                        }.toPersistentList()
                    )
                ),
            ),
            aGlobalSearchState(
                isSearchActive = true,
                currentTarget = GlobalSearchTarget.MESSAGES,
                queryState = TextFieldState("Query"),
                results = AsyncData.Success(GlobalSearchResults.MessageSearchResults(persistentListOf(
                    MessageSearchResultItem.Message(
                        messageSearchResult = aMessageSearchResult(eventId = EventId("\$eventId1:server.org")),
                        body = "A message with Query",
                        roomInfo = aRoomInfo(),
                        formattedTimestamp = "12:00",
                    ),
                    MessageSearchResultItem.Media(
                        messageSearchResult = aMessageSearchResult(eventId = EventId("\$eventId2:server.org")),
                        mediaContent = MediaSearchResultContent(
                            filename = "file.png",
                            extension = "PNG",
                            caption = "A caption containing Query",
                            formattedSize = "1 MB",
                            thumbnailSource = MediaSource("https://example.com/thumbnail.png"),
                            thumbnailType = AttachmentThumbnailType.Image,
                            blurhash = null,
                        ),
                        roomInfo = aRoomInfo(),
                        formattedTimestamp = "12:00",
                    ),
                ))),
            ),
            aGlobalSearchState(
                isSearchActive = true,
                currentTarget = GlobalSearchTarget.MESSAGES,
                queryState = TextFieldState("Query"),
                results = AsyncData.Success(GlobalSearchResults.MessageSearchResults(persistentListOf())),
            ),
        )
}

fun aRoomInfo(
    id: RoomId = RoomId("!roomId:server.org"),
    name: String? = "A room",
    rawName: String? = "A room raw name",
    topic: String? = "A room topic",
    avatarUrl: String? = "https://example.com/avatar.png",
    isPublic: Boolean = true,
    isDirect: Boolean = false,
    isEncrypted: Boolean = false,
    joinRule: JoinRule? = JoinRule.Public,
    isSpace: Boolean = false,
    successorRoom: SuccessorRoom? = null,
    isFavorite: Boolean = false,
    canonicalAlias: RoomAlias? = null,
    alternativeAliases: List<RoomAlias> = emptyList(),
    currentUserMembership: CurrentUserMembership = CurrentUserMembership.JOINED,
    inviter: RoomMember? = null,
    activeMembersCount: Long = 2,
    invitedMembersCount: Long = 1,
    joinedMembersCount: Long = 1,
    highlightCount: Long = 0,
    notificationCount: Long = 0,
    userDefinedNotificationMode: RoomNotificationMode? = null,
    hasRoomCall: Boolean = false,
    roomPowerLevels: RoomPowerLevels? = RoomPowerLevels(
        values = RoomPowerLevelsValues(
            ban = 0,
            invite = 0,
            kick = 0,
            eventsDefault = 0,
            stateDefault = 0,
            redactEvents = 0,
            roomName = 0,
            roomAvatar = 0,
            roomTopic = 0,
            spaceChild = 0,
            beacon = 0,
            beaconInfo = 0
        ),
        users = persistentMapOf(),
    ),
    activeRoomCallParticipants: List<UserId> = emptyList(),
    heroes: List<MatrixUser> = emptyList(),
    pinnedEventIds: List<EventId> = emptyList(),
    roomCreators: List<UserId> = emptyList(),
    isMarkedUnread: Boolean = false,
    numUnreadMessages: Long = 0,
    numUnreadNotifications: Long = 0,
    numUnreadMentions: Long = 0,
    historyVisibility: RoomHistoryVisibility = RoomHistoryVisibility.Joined,
    roomVersion: String? = "11",
    privilegedCreatorRole: Boolean = false,
    isLowPriority: Boolean = false,
    activeCallIntentConsensus: CallIntentConsensus = CallIntentConsensus.None,
    isDm: Boolean = false,
    fullyReadEventId: EventId? = null,
) = RoomInfo(
    id = id,
    name = name,
    rawName = rawName,
    topic = topic,
    avatarUrl = avatarUrl,
    isPublic = isPublic,
    isDirect = isDirect,
    isEncrypted = isEncrypted,
    joinRule = joinRule,
    isSpace = isSpace,
    successorRoom = successorRoom,
    isFavorite = isFavorite,
    canonicalAlias = canonicalAlias,
    alternativeAliases = alternativeAliases.toImmutableList(),
    currentUserMembership = currentUserMembership,
    inviter = inviter,
    activeMembersCount = activeMembersCount,
    invitedMembersCount = invitedMembersCount,
    joinedMembersCount = joinedMembersCount,
    highlightCount = highlightCount,
    notificationCount = notificationCount,
    userDefinedNotificationMode = userDefinedNotificationMode,
    hasRoomCall = hasRoomCall,
    roomPowerLevels = roomPowerLevels,
    activeRoomCallParticipants = activeRoomCallParticipants.toImmutableList(),
    heroes = heroes.toImmutableList(),
    pinnedEventIds = pinnedEventIds.toImmutableList(),
    creators = roomCreators.toImmutableList(),
    isMarkedUnread = isMarkedUnread,
    numUnreadMessages = numUnreadMessages,
    numUnreadNotifications = numUnreadNotifications,
    numUnreadMentions = numUnreadMentions,
    historyVisibility = historyVisibility,
    roomVersion = roomVersion,
    privilegedCreatorRole = privilegedCreatorRole,
    isLowPriority = isLowPriority,
    activeCallIntentConsensus = activeCallIntentConsensus,
    isDm = isDm,
    fullyReadEventId = fullyReadEventId,
)

fun aMessageContent(
    body: String = "body",
    inReplyTo: InReplyTo? = null,
    isEdited: Boolean = false,
    threadInfo: EventThreadInfo? = null,
    messageType: MessageType = TextMessageType(
        body = body,
        formatted = null
    )
) = MessageContent(
    body = body,
    inReplyTo = inReplyTo,
    isEdited = isEdited,
    threadInfo = threadInfo,
    type = messageType
)

internal fun aMessageSearchResult(
    roomId: RoomId = RoomId("!roomId:server.org"),
    eventId: EventId = EventId("\$eventId:server.org"),
    senderId: UserId = UserId("@user:server.org"),
    senderProfile: ProfileDetails = aProfileDetailsReady(),
    content: MessageContent = aMessageContent(),
    timestamp: Long = 0L,
) = MessageSearchResult(
    roomId = roomId,
    eventId = eventId,
    senderId = senderId,
    senderProfile = senderProfile,
    content = content,
    timestamp = timestamp,
)

internal fun aGlobalSearchState(
    isEnabled: Boolean = true,
    isSearchActive: Boolean = false,
    queryState: TextFieldState = TextFieldState(),
    currentTarget: GlobalSearchTarget = GlobalSearchTarget.ROOMS,
    results: AsyncData<GlobalSearchResults> = AsyncData.Uninitialized,
    eventSink: (GlobalSearchEvent) -> Unit = {},
) = GlobalSearchState(
    isEnabled = isEnabled,
    isSearchActive = isSearchActive,
    queryState = queryState,
    currentTarget = currentTarget,
    results = results,
    eventSink = eventSink
)
