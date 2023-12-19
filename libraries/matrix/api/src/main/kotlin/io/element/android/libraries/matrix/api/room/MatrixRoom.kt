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

package io.element.android.libraries.matrix.api.room

import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.SessionId
import io.element.android.libraries.matrix.api.core.TransactionId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.media.AudioInfo
import io.element.android.libraries.matrix.api.media.FileInfo
import io.element.android.libraries.matrix.api.media.ImageInfo
import io.element.android.libraries.matrix.api.media.MediaUploadHandler
import io.element.android.libraries.matrix.api.media.VideoInfo
import io.element.android.libraries.matrix.api.poll.PollKind
import io.element.android.libraries.matrix.api.room.location.AssetType
import io.element.android.libraries.matrix.api.timeline.MatrixTimeline
import io.element.android.libraries.matrix.api.widget.MatrixWidgetDriver
import io.element.android.libraries.matrix.api.widget.MatrixWidgetSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import java.io.Closeable
import java.io.File

interface MatrixRoom : Closeable {
    val sessionId: SessionId
    val roomId: RoomId
    val name: String?
    val displayName: String
    val alias: String?
    val alternativeAliases: List<String>
    val topic: String?
    val avatarUrl: String?
    val isEncrypted: Boolean
    val isDirect: Boolean
    val isPublic: Boolean
    val activeMemberCount: Long
    val joinedMemberCount: Long

    val roomInfoFlow: Flow<MatrixRoomInfo>

    /**
     * A one-to-one is a room with exactly 2 members.
     * See [the Matrix spec](https://spec.matrix.org/latest/client-server-api/#default-underride-rules).
     */
    val isOneToOne: Boolean get() = activeMemberCount == 2L

    /**
     * The current loaded members as a StateFlow.
     * Initial value is [MatrixRoomMembersState.Unknown].
     * To update them you should call [updateMembers].
     */
    val membersStateFlow: StateFlow<MatrixRoomMembersState>

    val roomNotificationSettingsStateFlow: StateFlow<MatrixRoomNotificationSettingsState>

    /**
     * Try to load the room members and update the membersFlow.
     */
    suspend fun updateMembers(): Result<Unit>

    suspend fun updateRoomNotificationSettings(): Result<Unit>

    val syncUpdateFlow: StateFlow<Long>

    val timeline: MatrixTimeline

    fun destroy()

    suspend fun subscribeToSync()

    suspend fun unsubscribeFromSync()

    suspend fun userDisplayName(userId: UserId): Result<String?>

    suspend fun userAvatarUrl(userId: UserId): Result<String?>

    suspend fun sendMessage(body: String, htmlBody: String?, mentions: List<Mention>): Result<Unit>

    suspend fun editMessage(originalEventId: EventId?, transactionId: TransactionId?, body: String, htmlBody: String?, mentions: List<Mention>): Result<Unit>

    suspend fun enterSpecialMode(eventId: EventId?): Result<Unit>

    suspend fun replyMessage(eventId: EventId, body: String, htmlBody: String?, mentions: List<Mention>): Result<Unit>

    suspend fun redactEvent(eventId: EventId, reason: String? = null): Result<Unit>

    suspend fun sendImage(file: File, thumbnailFile: File?, imageInfo: ImageInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler>

    suspend fun sendVideo(file: File, thumbnailFile: File?, videoInfo: VideoInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler>

    suspend fun sendAudio(file: File, audioInfo: AudioInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler>

    suspend fun sendFile(file: File, fileInfo: FileInfo, progressCallback: ProgressCallback?): Result<MediaUploadHandler>

    suspend fun toggleReaction(emoji: String, eventId: EventId): Result<Unit>

    suspend fun forwardEvent(eventId: EventId, roomIds: List<RoomId>): Result<Unit>

    suspend fun retrySendMessage(transactionId: TransactionId): Result<Unit>

    suspend fun cancelSend(transactionId: TransactionId): Result<Unit>

    suspend fun leave(): Result<Unit>

    suspend fun join(): Result<Unit>

    suspend fun inviteUserById(id: UserId): Result<Unit>

    suspend fun canUserInvite(userId: UserId): Result<Boolean>

    suspend fun canUserRedact(userId: UserId): Result<Boolean>

    suspend fun canUserSendState(userId: UserId, type: StateEventType): Result<Boolean>

    suspend fun canUserSendMessage(userId: UserId, type: MessageEventType): Result<Boolean>

    suspend fun canUserTriggerRoomNotification(userId: UserId): Result<Boolean>

    suspend fun canUserJoinCall(userId: UserId): Result<Boolean> =
        canUserSendState(userId, StateEventType.CALL_MEMBER)

    suspend fun updateAvatar(mimeType: String, data: ByteArray): Result<Unit>

    suspend fun removeAvatar(): Result<Unit>

    suspend fun setName(name: String): Result<Unit>

    suspend fun setTopic(topic: String): Result<Unit>

    suspend fun reportContent(eventId: EventId, reason: String, blockUserId: UserId?): Result<Unit>

    /**
     * Share a location message in the room.
     *
     * @param body A human readable textual representation of the location.
     * @param geoUri A geo URI (RFC 5870) representing the location e.g. `geo:51.5008,0.1247;u=35`.
     *  Respectively: latitude, longitude, and (optional) uncertainty.
     * @param description Optional description of the location to display to the user.
     * @param zoomLevel Optional zoom level to display the map at.
     * @param assetType Optional type of the location asset.
     *  Set to SENDER if sharing own location. Set to PIN if sharing any location.
     */
    suspend fun sendLocation(
        body: String,
        geoUri: String,
        description: String? = null,
        zoomLevel: Int? = null,
        assetType: AssetType? = null,
    ): Result<Unit>

    /**
     * Create a poll in the room.
     *
     * @param question The question to ask.
     * @param answers The list of answers.
     * @param maxSelections The maximum number of answers that can be selected.
     * @param pollKind The kind of poll to create.
     */
    suspend fun createPoll(
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit>

    /**
     * Edit a poll in the room.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param question The question to ask.
     * @param answers The list of answers.
     * @param maxSelections The maximum number of answers that can be selected.
     * @param pollKind The kind of poll to create.
     */
    suspend fun editPoll(
        pollStartId: EventId,
        question: String,
        answers: List<String>,
        maxSelections: Int,
        pollKind: PollKind,
    ): Result<Unit>

    /**
     * Send a response to a poll.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param answers The list of answer ids to send.
     */
    suspend fun sendPollResponse(pollStartId: EventId, answers: List<String>): Result<Unit>

    /**
     * Ends a poll in the room.
     *
     * @param pollStartId The event ID of the poll start event.
     * @param text Fallback text of the poll end event.
     */
    suspend fun endPoll(pollStartId: EventId, text: String): Result<Unit>

    suspend fun sendVoiceMessage(
        file: File,
        audioInfo: AudioInfo,
        waveform: List<Float>,
        progressCallback: ProgressCallback?
    ): Result<MediaUploadHandler>

    /**
     * Generates a Widget url to display in a [android.webkit.WebView] given the provided parameters.
     * @param widgetSettings The widget settings to use.
     * @param clientId The client id to use. It should be unique per app install.
     * @param languageTag The language tag to use. If null, the default language will be used.
     * @param theme The theme to use. If null, the default theme will be used.
     * @return The resulting url, or a failure.
     */
    suspend fun generateWidgetWebViewUrl(
        widgetSettings: MatrixWidgetSettings,
        clientId: String,
        languageTag: String? = null,
        theme: String? = null,
    ): Result<String>

    /**
     * Get a [MatrixWidgetDriver] for the provided [widgetSettings].
     * @param widgetSettings The widget settings to use.
     * @return The resulting [MatrixWidgetDriver], or a failure.
     */
    fun getWidgetDriver(widgetSettings: MatrixWidgetSettings): Result<MatrixWidgetDriver>

    fun pollHistory(): MatrixTimeline

    override fun close() = destroy()
}
