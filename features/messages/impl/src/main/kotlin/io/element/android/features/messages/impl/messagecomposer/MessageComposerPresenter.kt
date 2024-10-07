/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.messages.impl.messagecomposer

import android.Manifest
import android.annotation.SuppressLint
import android.net.Uri
import androidx.annotation.VisibleForTesting
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import im.vector.app.features.analytics.plan.Composer
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.error.sendAttachmentError
import io.element.android.features.messages.impl.draft.ComposerDraftService
import io.element.android.features.messages.impl.messagecomposer.suggestions.SuggestionsProcessor
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.utils.TextPillificationHelper
import io.element.android.libraries.architecture.Presenter
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarDispatcher
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarMessage
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.di.SingleIn
import io.element.android.libraries.featureflag.api.FeatureFlagService
import io.element.android.libraries.featureflag.api.FeatureFlags
import io.element.android.libraries.matrix.api.core.ProgressCallback
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.permalink.PermalinkBuilder
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.permalink.PermalinkParser
import io.element.android.libraries.matrix.api.room.IntentionalMention
import io.element.android.libraries.matrix.api.room.MatrixRoom
import io.element.android.libraries.matrix.api.room.draft.ComposerDraft
import io.element.android.libraries.matrix.api.room.draft.ComposerDraftType
import io.element.android.libraries.matrix.api.room.isDm
import io.element.android.libraries.matrix.api.timeline.TimelineException
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.matrix.ui.messages.reply.InReplyToDetails
import io.element.android.libraries.matrix.ui.messages.reply.map
import io.element.android.libraries.mediapickers.api.PickerProvider
import io.element.android.libraries.mediaupload.api.MediaSender
import io.element.android.libraries.mediaviewer.api.local.LocalMediaFactory
import io.element.android.libraries.permissions.api.PermissionsEvents
import io.element.android.libraries.permissions.api.PermissionsPresenter
import io.element.android.libraries.preferences.api.store.SessionPreferencesStore
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.MentionSpanProvider
import io.element.android.libraries.textcomposer.mentions.ResolvedSuggestion
import io.element.android.libraries.textcomposer.model.MarkdownTextEditorState
import io.element.android.libraries.textcomposer.model.Message
import io.element.android.libraries.textcomposer.model.MessageComposerMode
import io.element.android.libraries.textcomposer.model.Suggestion
import io.element.android.libraries.textcomposer.model.TextEditorState
import io.element.android.libraries.textcomposer.model.rememberMarkdownTextEditorState
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction
import io.element.android.wysiwyg.compose.RichTextEditorState
import io.element.android.wysiwyg.display.TextDisplay
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import timber.log.Timber
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
    private val sessionPreferencesStore: SessionPreferencesStore,
    private val localMediaFactory: LocalMediaFactory,
    private val mediaSender: MediaSender,
    private val snackbarDispatcher: SnackbarDispatcher,
    private val analyticsService: AnalyticsService,
    private val messageComposerContext: DefaultMessageComposerContext,
    private val richTextEditorStateFactory: RichTextEditorStateFactory,
    private val roomAliasSuggestionsDataSource: RoomAliasSuggestionsDataSource,
    private val permalinkParser: PermalinkParser,
    private val permalinkBuilder: PermalinkBuilder,
    permissionsPresenterFactory: PermissionsPresenter.Factory,
    private val timelineController: TimelineController,
    private val draftService: ComposerDraftService,
    private val mentionSpanProvider: MentionSpanProvider,
    private val pillificationHelper: TextPillificationHelper,
    private val roomMemberProfilesCache: RoomMemberProfilesCache,
    private val suggestionsProcessor: SuggestionsProcessor,
) : Presenter<MessageComposerState> {
    private val cameraPermissionPresenter = permissionsPresenterFactory.create(Manifest.permission.CAMERA)
    private var pendingEvent: MessageComposerEvents? = null
    private val suggestionSearchTrigger = MutableStateFlow<Suggestion?>(null)

    // Used to disable some UI related elements in tests
    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var isTesting: Boolean = false

    @VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
    internal var showTextFormatting: Boolean by mutableStateOf(false)

    @OptIn(FlowPreview::class)
    @SuppressLint("UnsafeOptInUsageError")
    @Composable
    override fun present(): MessageComposerState {
        val localCoroutineScope = rememberCoroutineScope()

        val richTextEditorState = richTextEditorStateFactory.remember()
        if (isTesting) {
            richTextEditorState.isReadyToProcessActions = true
        }
        val markdownTextEditorState = rememberMarkdownTextEditorState(initialText = null, initialFocus = false)
        var isMentionsEnabled by remember { mutableStateOf(false) }
        var isRoomAliasSuggestionsEnabled by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            isMentionsEnabled = featureFlagService.isFeatureEnabled(FeatureFlags.Mentions)
            isRoomAliasSuggestionsEnabled = featureFlagService.isFeatureEnabled(FeatureFlags.RoomAliasSuggestions)
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
        val ongoingSendAttachmentJob = remember { mutableStateOf<Job?>(null) }

        var showAttachmentSourcePicker: Boolean by remember { mutableStateOf(false) }

        val sendTypingNotifications by sessionPreferencesStore.isSendTypingNotificationsEnabled().collectAsState(initial = true)

        val roomAliasSuggestions by roomAliasSuggestionsDataSource.getAllRoomAliasSuggestions().collectAsState(initial = emptyList())

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

        val suggestions = remember { mutableStateListOf<ResolvedSuggestion>() }
        LaunchedEffect(isMentionsEnabled) {
            if (!isMentionsEnabled) return@LaunchedEffect
            val currentUserId = room.sessionId

            suspend fun canSendRoomMention(): Boolean {
                val userCanSendAtRoom = room.canUserTriggerRoomNotification(currentUserId).getOrDefault(false)
                return !room.isDm && userCanSendAtRoom
            }

            // This will trigger a search immediately when `@` is typed
            val mentionStartTrigger = suggestionSearchTrigger.filter { it?.text.isNullOrEmpty() }
            // This will start a search when the user changes the text after the `@` with a debounce to prevent too much wasted work
            val mentionCompletionTrigger = suggestionSearchTrigger.debounce(0.3.seconds).filter { !it?.text.isNullOrEmpty() }
            merge(mentionStartTrigger, mentionCompletionTrigger)
                .combine(room.membersStateFlow) { suggestion, roomMembersState ->
                    suggestions.clear()
                    val result = suggestionsProcessor.process(
                        suggestion = suggestion,
                        roomMembersState = roomMembersState,
                        roomAliasSuggestions = if (isRoomAliasSuggestionsEnabled) roomAliasSuggestions else emptyList(),
                        currentUserId = currentUserId,
                        canSendRoomMention = ::canSendRoomMention,
                    )
                    if (result.isNotEmpty()) {
                        suggestions.addAll(result)
                    }
                }
                .collect()
        }

        DisposableEffect(Unit) {
            // Declare that the user is not typing anymore when the composer is disposed
            onDispose {
                appCoroutineScope.launch {
                    if (sendTypingNotifications) {
                        room.typingNotice(false)
                    }
                }
            }
        }

        val textEditorState by rememberUpdatedState(
            if (showTextFormatting) {
                TextEditorState.Rich(richTextEditorState)
            } else {
                TextEditorState.Markdown(markdownTextEditorState)
            }
        )

        LaunchedEffect(Unit) {
            val draft = draftService.loadDraft(room.roomId, isVolatile = false)
            if (draft != null) {
                applyDraft(draft, markdownTextEditorState, richTextEditorState)
            }
        }

        fun handleEvents(event: MessageComposerEvents) {
            when (event) {
                MessageComposerEvents.ToggleFullScreenState -> isFullScreen.value = !isFullScreen.value
                MessageComposerEvents.CloseSpecialMode -> {
                    if (messageComposerContext.composerMode is MessageComposerMode.Edit) {
                        localCoroutineScope.launch {
                            resetComposer(markdownTextEditorState, richTextEditorState, fromEdit = true)
                        }
                    } else {
                        messageComposerContext.composerMode = MessageComposerMode.Normal
                    }
                }
                is MessageComposerEvents.SendMessage -> {
                    appCoroutineScope.sendMessage(
                        markdownTextEditorState = markdownTextEditorState,
                        richTextEditorState = richTextEditorState,
                    )
                }
                is MessageComposerEvents.SendUri -> appCoroutineScope.sendAttachment(
                    attachment = Attachment.Media(
                        localMedia = localMediaFactory.createFromUri(
                            uri = event.uri,
                            mimeType = null,
                            name = null,
                            formattedFileSize = null
                        ),
                        compressIfPossible = true
                    ),
                    attachmentState = attachmentsState,
                )
                is MessageComposerEvents.SetMode -> {
                    localCoroutineScope.setMode(event.composerMode, markdownTextEditorState, richTextEditorState)
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
                    localCoroutineScope.toggleTextFormatting(event.enabled, markdownTextEditorState, richTextEditorState)
                }
                is MessageComposerEvents.Error -> {
                    analyticsService.trackError(event.error)
                }
                is MessageComposerEvents.TypingNotice -> {
                    if (sendTypingNotifications) {
                        localCoroutineScope.launch {
                            room.typingNotice(event.isTyping)
                        }
                    }
                }
                is MessageComposerEvents.SuggestionReceived -> {
                    suggestionSearchTrigger.value = event.suggestion
                }
                is MessageComposerEvents.InsertSuggestion -> {
                    localCoroutineScope.launch {
                        if (showTextFormatting) {
                            when (val suggestion = event.resolvedSuggestion) {
                                is ResolvedSuggestion.AtRoom -> {
                                    richTextEditorState.insertAtRoomMentionAtSuggestion()
                                }
                                is ResolvedSuggestion.Member -> {
                                    val text = suggestion.roomMember.userId.value
                                    val link = permalinkBuilder.permalinkForUser(suggestion.roomMember.userId).getOrNull() ?: return@launch
                                    richTextEditorState.insertMentionAtSuggestion(text = text, link = link)
                                }
                                is ResolvedSuggestion.Alias -> {
                                    val text = suggestion.roomAlias.value
                                    val link = permalinkBuilder.permalinkForRoomAlias(suggestion.roomAlias).getOrNull() ?: return@launch
                                    richTextEditorState.insertMentionAtSuggestion(text = text, link = link)
                                }
                            }
                        } else if (markdownTextEditorState.currentSuggestion != null) {
                            markdownTextEditorState.insertSuggestion(
                                resolvedSuggestion = event.resolvedSuggestion,
                                mentionSpanProvider = mentionSpanProvider,
                                permalinkBuilder = permalinkBuilder,
                            )
                            suggestionSearchTrigger.value = null
                        }
                    }
                }
                MessageComposerEvents.SaveDraft -> {
                    val draft = createDraftFromState(markdownTextEditorState, richTextEditorState)
                    appCoroutineScope.updateDraft(draft, isVolatile = false)
                }
            }
        }

        val mentionSpanTheme = LocalMentionSpanTheme.current
        val resolveMentionDisplay = remember(mentionSpanTheme) {
            { text: String, url: String ->
                val permalinkData = permalinkParser.parse(url)
                if (permalinkData is PermalinkData.UserLink) {
                    val displayNameOrId = roomMemberProfilesCache.getDisplayName(permalinkData.userId) ?: permalinkData.userId.value
                    val mentionSpan = mentionSpanProvider.getMentionSpanFor(displayNameOrId, url)
                    mentionSpan.update(mentionSpanTheme)
                    TextDisplay.Custom(mentionSpan)
                } else {
                    val mentionSpan = mentionSpanProvider.getMentionSpanFor(text, url)
                    mentionSpan.update(mentionSpanTheme)
                    TextDisplay.Custom(mentionSpan)
                }
            }
        }
        return MessageComposerState(
            textEditorState = textEditorState,
            isFullScreen = isFullScreen.value,
            mode = messageComposerContext.composerMode,
            showAttachmentSourcePicker = showAttachmentSourcePicker,
            showTextFormatting = showTextFormatting,
            canShareLocation = canShareLocation.value,
            canCreatePoll = canCreatePoll.value,
            attachmentsState = attachmentsState.value,
            suggestions = suggestions.toPersistentList(),
            resolveMentionDisplay = resolveMentionDisplay,
            eventSink = { handleEvents(it) },
        )
    }

    private fun CoroutineScope.sendMessage(
        markdownTextEditorState: MarkdownTextEditorState,
        richTextEditorState: RichTextEditorState,
    ) = launch {
        val message = currentComposerMessage(markdownTextEditorState, richTextEditorState, withMentions = true)
        val capturedMode = messageComposerContext.composerMode
        // Reset composer right away
        resetComposer(markdownTextEditorState, richTextEditorState, fromEdit = capturedMode is MessageComposerMode.Edit)
        when (capturedMode) {
            is MessageComposerMode.Normal -> room.sendMessage(
                body = message.markdown,
                htmlBody = message.html,
                intentionalMentions = message.intentionalMentions
            )
            is MessageComposerMode.Edit -> {
                val eventId = capturedMode.eventId
                val transactionId = capturedMode.transactionId
                timelineController.invokeOnCurrentTimeline {
                    // First try to edit the message in the current timeline
                    editMessage(eventId, transactionId, message.markdown, message.html, message.intentionalMentions)
                        .onFailure { cause ->
                            if (cause is TimelineException.EventNotFound && eventId != null) {
                                // if the event is not found in the timeline, try to edit the message directly
                                room.editMessage(eventId, message.markdown, message.html, message.intentionalMentions)
                            }
                        }
                }
            }

            is MessageComposerMode.Reply -> {
                timelineController.invokeOnCurrentTimeline {
                    replyMessage(capturedMode.eventId, message.markdown, message.html, message.intentionalMentions)
                }
            }
        }
        analyticsService.capture(
            Composer(
                inThread = capturedMode.inThread,
                isEditing = capturedMode.isEditing,
                isReply = capturedMode.isReply,
                // Set proper type when we'll be sending other types of messages.
                messageType = Composer.MessageType.Text,
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
        mediaSender.sendMedia(
            uri = uri,
            mimeType = mimeType,
            compressIfPossible = false,
            progressCallback = progressCallback
        ).getOrThrow()
    }
        .onSuccess {
            attachmentState.value = AttachmentsState.None
        }
        .onFailure { cause ->
            Timber.e(cause, "Failed to send attachment")
            attachmentState.value = AttachmentsState.None
            if (cause is CancellationException) {
                throw cause
            } else {
                val snackbarMessage = SnackbarMessage(sendAttachmentError(cause))
                snackbarDispatcher.post(snackbarMessage)
            }
        }

    private fun CoroutineScope.updateDraft(
        draft: ComposerDraft?,
        isVolatile: Boolean,
    ) = launch {
        draftService.updateDraft(
            roomId = room.roomId,
            draft = draft,
            isVolatile = isVolatile
        )
    }

    private suspend fun applyDraft(
        draft: ComposerDraft,
        markdownTextEditorState: MarkdownTextEditorState,
        richTextEditorState: RichTextEditorState,
    ) {
        val htmlText = draft.htmlText
        val markdownText = draft.plainText
        if (htmlText != null) {
            showTextFormatting = true
            setText(htmlText, markdownTextEditorState, richTextEditorState, requestFocus = true)
        } else {
            showTextFormatting = false
            setText(markdownText, markdownTextEditorState, richTextEditorState, requestFocus = true)
        }
        when (val draftType = draft.draftType) {
            ComposerDraftType.NewMessage -> messageComposerContext.composerMode = MessageComposerMode.Normal
            is ComposerDraftType.Edit -> messageComposerContext.composerMode = MessageComposerMode.Edit(
                eventId = draftType.eventId,
                transactionId = null,
                content = htmlText ?: markdownText
            )
            is ComposerDraftType.Reply -> {
                messageComposerContext.composerMode = MessageComposerMode.Reply(
                    replyToDetails = InReplyToDetails.Loading(draftType.eventId),
                    // I guess it's fine to always render the image when restoring a draft
                    hideImage = false
                )
                timelineController.invokeOnCurrentTimeline {
                    val replyToDetails = loadReplyDetails(draftType.eventId).map(permalinkParser)
                    messageComposerContext.composerMode = MessageComposerMode.Reply(
                        replyToDetails = replyToDetails,
                        // I guess it's fine to always render the image when restoring a draft
                        hideImage = false
                    )
                }
            }
        }
    }

    private fun createDraftFromState(
        markdownTextEditorState: MarkdownTextEditorState,
        richTextEditorState: RichTextEditorState,
    ): ComposerDraft? {
        val message = currentComposerMessage(markdownTextEditorState, richTextEditorState, withMentions = false)
        val draftType = when (val mode = messageComposerContext.composerMode) {
            is MessageComposerMode.Normal -> ComposerDraftType.NewMessage
            is MessageComposerMode.Edit -> {
                mode.eventId?.let { eventId -> ComposerDraftType.Edit(eventId) }
            }
            is MessageComposerMode.Reply -> ComposerDraftType.Reply(mode.eventId)
        }
        return if (draftType == null || message.markdown.isBlank()) {
            null
        } else {
            ComposerDraft(
                draftType = draftType,
                htmlText = message.html,
                plainText = message.markdown,
            )
        }
    }

    private fun currentComposerMessage(
        markdownTextEditorState: MarkdownTextEditorState,
        richTextEditorState: RichTextEditorState,
        withMentions: Boolean,
    ): Message {
        return if (showTextFormatting) {
            val html = richTextEditorState.messageHtml
            val markdown = richTextEditorState.messageMarkdown
            val mentions = richTextEditorState.mentionsState
                .takeIf { withMentions }
                ?.let { state ->
                    buildList {
                        if (state.hasAtRoomMention) {
                            add(IntentionalMention.Room)
                        }
                        for (userId in state.userIds) {
                            add(IntentionalMention.User(UserId(userId)))
                        }
                    }
                }
                .orEmpty()
            Message(html = html, markdown = markdown, intentionalMentions = mentions)
        } else {
            val markdown = markdownTextEditorState.getMessageMarkdown(permalinkBuilder)
            val mentions = if (withMentions) {
                markdownTextEditorState.getMentions()
            } else {
                emptyList()
            }
            Message(html = null, markdown = markdown, intentionalMentions = mentions)
        }
    }

    private fun CoroutineScope.toggleTextFormatting(
        enabled: Boolean,
        markdownTextEditorState: MarkdownTextEditorState,
        richTextEditorState: RichTextEditorState
    ) = launch {
        showTextFormatting = enabled
        if (showTextFormatting) {
            val markdown = markdownTextEditorState.getMessageMarkdown(permalinkBuilder)
            richTextEditorState.setMarkdown(markdown)
            richTextEditorState.requestFocus()
            analyticsService.captureInteraction(Interaction.Name.MobileRoomComposerFormattingEnabled)
        } else {
            val markdown = richTextEditorState.messageMarkdown
            val pilliefiedMarkdown = pillificationHelper.pillify(markdown)
            markdownTextEditorState.text.update(pilliefiedMarkdown, true)
            // Give some time for the focus of the previous editor to be cleared
            delay(100)
            markdownTextEditorState.requestFocusAction()
        }
    }

    private fun CoroutineScope.setMode(
        newComposerMode: MessageComposerMode,
        markdownTextEditorState: MarkdownTextEditorState,
        richTextEditorState: RichTextEditorState,
    ) = launch {
        val currentComposerMode = messageComposerContext.composerMode
        when (newComposerMode) {
            is MessageComposerMode.Edit -> {
                if (currentComposerMode !is MessageComposerMode.Edit) {
                    val draft = createDraftFromState(markdownTextEditorState, richTextEditorState)
                    updateDraft(draft, isVolatile = true).join()
                }
                setText(newComposerMode.content, markdownTextEditorState, richTextEditorState)
            }
            else -> {
                // When coming from edit, just clear the composer as it'd be weird to reset a volatile draft in this scenario.
                if (currentComposerMode is MessageComposerMode.Edit) {
                    setText("", markdownTextEditorState, richTextEditorState)
                }
            }
        }
        messageComposerContext.composerMode = newComposerMode
    }

    private suspend fun resetComposer(
        markdownTextEditorState: MarkdownTextEditorState,
        richTextEditorState: RichTextEditorState,
        fromEdit: Boolean,
    ) {
        // Use the volatile draft only when coming from edit mode otherwise.
        val draft = draftService.loadDraft(room.roomId, isVolatile = true).takeIf { fromEdit }
        if (draft != null) {
            applyDraft(draft, markdownTextEditorState, richTextEditorState)
        } else {
            setText("", markdownTextEditorState, richTextEditorState)
            messageComposerContext.composerMode = MessageComposerMode.Normal
        }
    }

    private suspend fun setText(
        content: String,
        markdownTextEditorState: MarkdownTextEditorState,
        richTextEditorState: RichTextEditorState,
        requestFocus: Boolean = false,
    ) {
        if (showTextFormatting) {
            richTextEditorState.setHtml(content)
            if (requestFocus) {
                richTextEditorState.requestFocus()
            }
        } else {
            if (content.isEmpty()) {
                markdownTextEditorState.selection = IntRange.EMPTY
            }
            val pillifiedContent = pillificationHelper.pillify(content)
            markdownTextEditorState.text.update(pillifiedContent, true)
            if (requestFocus) {
                markdownTextEditorState.requestFocusAction()
            }
        }
    }
}
