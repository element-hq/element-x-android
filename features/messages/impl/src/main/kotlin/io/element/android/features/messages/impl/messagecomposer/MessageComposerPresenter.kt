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

package io.element.android.features.messages.impl.messagecomposer

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import im.vector.app.features.analytics.plan.Composer
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.error.sendAttachmentError
import io.element.android.features.messages.impl.media.local.LocalMediaFactory
import io.element.android.features.messages.impl.mentions.MentionSuggestion
import io.element.android.features.messages.impl.mentions.MentionSuggestionsProcessor
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.Mention
import io.element.android.libraries.matrix.api.user.CurrentSessionIdHolder
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.textcomposer.model.Message
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.wysiwyg.compose.RichTextEditorState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration.Companion.seconds
import io.element.android.libraries.core.mimetype.MimeTypes.Any as AnyMimeTypes

@SingleIn(RoomScope::class)
class MessageComposerPresenter @Inject constructor(
    private val appCoroutineScope: CoroutineScope,
    private val room: MatrixRoom,
    private val mediaPickerProvider: PickerProvider,
    private val featureFlagService: FeatureFlagService,
    private val localMediaFactory: LocalMediaFactory,
    private val mediaSender: MediaSender,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val analyticsService: AnalyticsService,
    private val messageComposerContext: MessageComposerContextImpl,
    private val richTextEditorStateFactory: RichTextEditorStateFactory,
    private val currentSessionIdHolder: CurrentSessionIdHolder,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
) : Presenter<MessageComposerState> {

    private val cameraPermissionPresenter = permissionsPresenterFactory.create(Manifest.permission.CAMERA)
    private var pendingEvent: MessageComposerEvents? = null

    private val suggestionSearchTrigger = MutableStateFlow<Suggestion?>(null)

    @OptIn(FlowPreview::class)
    @SuppressLint("UnsafeOptInUsageError")
    @Composable
    override fun present(): MessageComposerState {
        val localCoroutineScope = rememberCoroutineScope()

        var isMentionsEnabled by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isMentionsEnabled = featureFlagService.isFeatureEnabled(FeatureFlags.Mentions)
        }

        val cameraPermissionState = cameraPermissionPresenter.present()
        val attachmentsState = remember {
            mutableStateOf<AttachmentsState>(AttachmentsState.None)
        }

        val canShareLocation = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            canShareLocation.value = featureFlagService.isFeatureEnabled(FeatureFlags.LocationSharing)
        }

        val canCreatePoll = remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            canCreatePoll.value = featureFlagService.isFeatureEnabled(FeatureFlags.Polls)
        }

        val galleryMediaPicker = mediaPickerProvider.registerGalleryPicker { uri, mimeType ->
            handlePickedMedia(attachmentsState, uri, mimeType)
        }
        val filesPicker = mediaPickerProvider.registerFilePicker(AnyMimeTypes) { uri ->
            handlePickedMedia(attachmentsState, uri, compressIfPossible = false)
        }
        val cameraPhotoPicker = mediaPickerProvider.registerCameraPhotoPicker { uri ->
            handlePickedMedia(attachmentsState, uri, MimeTypes.IMAGE_JPEG)
        }
        val cameraVideoPicker = mediaPickerProvider.registerCameraVideoPicker { uri ->
            handlePickedMedia(attachmentsState, uri, MimeTypes.VIDEO_MP4)
        }
        val isFullScreen = rememberSaveable {
            mutableStateOf(false)
        }
        val richTextEditorState = richTextEditorStateFactory.create()
        val ongoingSendAttachmentJob = remember { mutableStateOf<Job?>(null) }

        var showAttachmentSourcePicker: Boolean by remember { mutableStateOf(false) }
        var showTextFormatting: Boolean by remember { mutableStateOf(false) }

        LaunchedEffect(messageComposerContext.composerMode) {
            when (val modeValue = messageComposerContext.composerMode) {
                is MessageComposerMode.Edit ->
                    richTextEditorState.setHtml(modeValue.defaultContent)
                else -> Unit
            }
        }

        LaunchedEffect(attachmentsState.value) {
            when (val attachmentStateValue = attachmentsState.value) {
                is AttachmentsState.Sending.Processing -> {
                    ongoingSendAttachmentJob.value = localCoroutineScope.sendAttachment(
                        attachmentStateValue.attachments.first(),
                        attachmentsState,
                    )
                }
                else -> Unit
            }
        }

        LaunchedEffect(cameraPermissionState.permissionGranted) {
            if (cameraPermissionState.permissionGranted) {
                when (pendingEvent) {
                    is MessageComposerEvents.PickAttachmentSource.PhotoFromCamera -> cameraPhotoPicker.launch()
                    is MessageComposerEvents.PickAttachmentSource.VideoFromCamera -> cameraVideoPicker.launch()
                    else -> Unit
                }
                pendingEvent = null
            }
        }

        val memberSuggestions = remember { mutableStateListOf<MentionSuggestion>() }
        LaunchedEffect(isMentionsEnabled) {
            if (!isMentionsEnabled) return@LaunchedEffect
            val currentUserId = currentSessionIdHolder.current

            suspend fun canSendRoomMention(): Boolean {
                val roomIsDm = room.isDirect && room.isOneToOne
                val userCanSendAtRoom = room.canUserTriggerRoomNotification(currentUserId).getOrDefault(false)
                return !roomIsDm && userCanSendAtRoom
            }

            // This will trigger a search immediately when `@` is typed
            val mentionStartTrigger = suggestionSearchTrigger.filter { it?.text.isNullOrEmpty() }
            // This will start a search when the user changes the text after the `@` with a debounce to prevent too much wasted work
            val mentionCompletionTrigger = suggestionSearchTrigger.filter { !it?.text.isNullOrEmpty() }.debounce(0.3.seconds)
            merge(mentionStartTrigger, mentionCompletionTrigger)
                .combine(room.membersStateFlow) { suggestion, roomMembersState ->
                    memberSuggestions.clear()
                    val result = MentionSuggestionsProcessor.process(
                        suggestion = suggestion,
                        roomMembersState = roomMembersState,
                        currentUserId = currentUserId,
                        canSendRoomMention = ::canSendRoomMention,
                    )
                    if (result.isNotEmpty()) {
                        memberSuggestions.addAll(result)
                    }
                }
                .collect()
        }

        fun handleEvents(event: MessageComposerEvents) {
            when (event) {
                MessageComposerEvents.ToggleFullScreenState -> isFullScreen.value = !isFullScreen.value
                MessageComposerEvents.CloseSpecialMode -> {
                    if (messageComposerContext.composerMode is MessageComposerMode.Edit) {
                        localCoroutineScope.launch {
                            richTextEditorState.setHtml("")
                        }
                    }
                    messageComposerContext.composerMode = MessageComposerMode.Normal
                }
                is MessageComposerEvents.SendMessage -> appCoroutineScope.sendMessage(
                    message = event.message,
                    updateComposerMode = { messageComposerContext.composerMode = it },
                    richTextEditorState = richTextEditorState,
                )
                is MessageComposerEvents.SetMode -> {
                    messageComposerContext.composerMode = event.composerMode
                    when (event.composerMode) {
                        is MessageComposerMode.Reply -> event.composerMode.eventId
                        is MessageComposerMode.Edit -> event.composerMode.eventId
                        is MessageComposerMode.Normal -> null
                        is MessageComposerMode.Quote -> null
                    }.let { relatedEventId ->
                        appCoroutineScope.launch {
                            room.enterSpecialMode(relatedEventId)
                        }
                    }
                }
                MessageComposerEvents.AddAttachment -> localCoroutineScope.launch {
                    showAttachmentSourcePicker = true
                }
                MessageComposerEvents.DismissAttachmentMenu -> showAttachmentSourcePicker = false
                MessageComposerEvents.PickAttachmentSource.FromGallery -> localCoroutineScope.launch {
                    showAttachmentSourcePicker = false
                    galleryMediaPicker.launch()
                }
                MessageComposerEvents.PickAttachmentSource.FromFiles -> localCoroutineScope.launch {
                    showAttachmentSourcePicker = false
                    filesPicker.launch()
                }
                MessageComposerEvents.PickAttachmentSource.PhotoFromCamera -> localCoroutineScope.launch {
                    showAttachmentSourcePicker = false
                    if (cameraPermissionState.permissionGranted) {
                        cameraPhotoPicker.launch()
                    } else {
                        pendingEvent = event
                        cameraPermissionState.eventSink(PermissionsEvents.RequestPermissions)
                    }
                }
                MessageComposerEvents.PickAttachmentSource.VideoFromCamera -> localCoroutineScope.launch {
                    showAttachmentSourcePicker = false
                    if (cameraPermissionState.permissionGranted) {
                        cameraVideoPicker.launch()
                    } else {
                        pendingEvent = event
                        cameraPermissionState.eventSink(PermissionsEvents.RequestPermissions)
                    }
                }
                MessageComposerEvents.PickAttachmentSource.Location -> {
                    showAttachmentSourcePicker = false
                    // Navigation to the location picker screen is done at the view layer
                }
                MessageComposerEvents.PickAttachmentSource.Poll -> {
                    showAttachmentSourcePicker = false
                    // Navigation to the create poll screen is done at the view layer
                }
                is MessageComposerEvents.CancelSendAttachment -> {
                    ongoingSendAttachmentJob.value?.let {
                        it.cancel()
                        ongoingSendAttachmentJob.value == null
                    }
                }
                is MessageComposerEvents.ToggleTextFormatting -> {
                    showAttachmentSourcePicker = false
                    showTextFormatting = event.enabled
                }
                is MessageComposerEvents.Error -> {
                    analyticsService.trackError(event.error)
                }
                is MessageComposerEvents.SuggestionReceived -> {
                    suggestionSearchTrigger.value = event.suggestion
                }
                is MessageComposerEvents.InsertMention -> {
                    localCoroutineScope.launch {
                        when (val mention = event.mention) {
                            is MentionSuggestion.Room -> {
                                richTextEditorState.insertAtRoomMentionAtSuggestion()
                            }
                            is MentionSuggestion.Member -> {
                                val text = mention.roomMember.displayName?.prependIndent("@") ?: mention.roomMember.userId.value
                                val link = PermalinkBuilder.permalinkForUser(mention.roomMember.userId).getOrNull() ?: return@launch
                                richTextEditorState.insertMentionAtSuggestion(text = text, link = link)
                            }
                        }
                    }
                }
            }
        }

        return MessageComposerState(
            richTextEditorState = richTextEditorState,
            isFullScreen = isFullScreen.value,
            mode = messageComposerContext.composerMode,
            showAttachmentSourcePicker = showAttachmentSourcePicker,
            showTextFormatting = showTextFormatting,
            canShareLocation = canShareLocation.value,
            canCreatePoll = canCreatePoll.value,
            attachmentsState = attachmentsState.value,
            memberSuggestions = memberSuggestions.toPersistentList(),
            currentUserId = currentSessionIdHolder.current,
            eventSink = { handleEvents(it) }
        )
    }

    private fun CoroutineScope.sendMessage(
        message: Message,
        updateComposerMode: (newComposerMode: MessageComposerMode) -> Unit,
        richTextEditorState: RichTextEditorState,
    ) = launch {
        val capturedMode = messageComposerContext.composerMode
        val mentions = richTextEditorState.mentionsState?.let { state ->
            buildList {
                if (state.hasAtRoomMention) {
                    add(Mention.AtRoom)
                }
                for (userId in state.userIds) {
                    add(Mention.User(userId))
                }
            }
        }.orEmpty()
        // Reset composer right away
        richTextEditorState.setHtml("")
        updateComposerMode(MessageComposerMode.Normal)
        when (capturedMode) {
            is MessageComposerMode.Normal -> room.sendMessage(body = message.markdown, htmlBody = message.html, mentions = mentions)
            is MessageComposerMode.Edit -> {
                val eventId = capturedMode.eventId
                val transactionId = capturedMode.transactionId
                room.editMessage(eventId, transactionId, message.markdown, message.html, mentions)
            }

            is MessageComposerMode.Quote -> TODO()
            is MessageComposerMode.Reply -> room.replyMessage(
                capturedMode.eventId,
                message.markdown,
                message.html,
                mentions
            )
        }
        analyticsService.capture(
            Composer(
                inThread = capturedMode.inThread,
                isEditing = capturedMode.isEditing,
                isReply = capturedMode.isReply,
                messageType = Composer.MessageType.Text, // Set proper type when we'll be sending other types of messages.
            )
        )
    }

    private fun CoroutineScope.sendAttachment(
        attachment: Attachment,
        attachmentState: MutableState<AttachmentsState>,
    ) = when (attachment) {
        is Attachment.Media -> {
            launch {
                sendMedia(
                    uri = attachment.localMedia.uri,
                    mimeType = attachment.localMedia.info.mimeType,
                    attachmentState = attachmentState,
                )
            }
        }
    }

    @UnstableApi
    private fun handlePickedMedia(
        attachmentsState: MutableState<AttachmentsState>,
        uri: Uri?,
        mimeType: String? = null,
        compressIfPossible: Boolean = true,
    ) {
        if (uri == null) {
            attachmentsState.value = AttachmentsState.None
            return
        }
        val localMedia = localMediaFactory.createFromUri(
            uri = uri,
            mimeType = mimeType,
            name = null,
            formattedFileSize = null
        )
        val mediaAttachment = Attachment.Media(localMedia, compressIfPossible)
        val isPreviewable = when {
            MimeTypes.isImage(localMedia.info.mimeType) -> true
            MimeTypes.isVideo(localMedia.info.mimeType) -> true
            MimeTypes.isAudio(localMedia.info.mimeType) -> true
            else -> false
        }
        attachmentsState.value = if (isPreviewable) {
            AttachmentsState.Previewing(persistentListOf(mediaAttachment))
        } else {
            AttachmentsState.Sending.Processing(persistentListOf(mediaAttachment))
        }
    }

    private suspend fun sendMedia(
        uri: Uri,
        mimeType: String,
        attachmentState: MutableState<AttachmentsState>,
    ) = runCatching {
        val context = coroutineContext
        val progressCallback = object : ProgressCallback {
            override fun onProgress(current: Long, total: Long) {
                if (context.isActive) {
                    attachmentState.value = AttachmentsState.Sending.Uploading(current.toFloat() / total.toFloat())
                }
            }
        }
        mediaSender.sendMedia(uri, mimeType, compressIfPossible = false, progressCallback).getOrThrow()
    }
        .onSuccess {
            attachmentState.value = AttachmentsState.None
        }
        .onFailure { cause ->
            attachmentState.value = AttachmentsState.None
            if (cause is CancellationException) {
                throw cause
            } else {
                val snackbarMessage = SnackbarMessage(sendAttachmentError(cause))
                snackbarDispatcher.post(snackbarMessage)
            }
        }
}

