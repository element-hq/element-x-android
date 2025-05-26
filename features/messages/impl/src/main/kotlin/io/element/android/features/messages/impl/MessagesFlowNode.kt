/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.bumble.appyx.core.lifecycle.subscribe
import com.bumble.appyx.core.modality.BuildContext
import com.bumble.appyx.core.node.Node
import com.bumble.appyx.core.node.node
import com.bumble.appyx.core.plugin.Plugin
import com.bumble.appyx.core.plugin.plugins
import com.bumble.appyx.navmodel.backstack.BackStack
import com.bumble.appyx.navmodel.backstack.operation.push
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import im.vector.app.features.analytics.plan.Interaction
import io.element.android.anvilannotations.ContributesNode
import io.element.android.features.call.api.CallType
import io.element.android.features.call.api.ElementCallEntryPoint
import io.element.android.features.knockrequests.api.list.KnockRequestsListEntryPoint
import io.element.android.features.location.api.Location
import io.element.android.features.location.api.LocationService
import io.element.android.features.location.api.SendLocationEntryPoint
import io.element.android.features.location.api.ShowLocationEntryPoint
import io.element.android.features.messages.api.MessagesEntryPoint
import io.element.android.features.messages.impl.attachments.Attachment
import io.element.android.features.messages.impl.attachments.preview.AttachmentsPreviewNode
import io.element.android.features.messages.impl.forward.ForwardMessagesNode
import io.element.android.features.messages.impl.pinned.PinnedEventsTimelineProvider
import io.element.android.features.messages.impl.pinned.list.PinnedMessagesListNode
import io.element.android.features.messages.impl.report.ReportMessageNode
import io.element.android.features.messages.impl.timeline.TimelineController
import io.element.android.features.messages.impl.timeline.debug.EventDebugInfoNode
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemAudioContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemEventContentWithAttachment
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemFileContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemImageContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemLocationContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVideoContent
import io.element.android.features.messages.impl.timeline.model.event.TimelineItemVoiceContent
import io.element.android.features.messages.impl.timeline.model.event.duration
import io.element.android.features.poll.api.create.CreatePollEntryPoint
import io.element.android.features.poll.api.create.CreatePollMode
import io.element.android.libraries.architecture.BackstackWithOverlayBox
import io.element.android.libraries.architecture.BaseFlowNode
import io.element.android.libraries.architecture.createNode
import io.element.android.libraries.architecture.overlay.Overlay
import io.element.android.libraries.architecture.overlay.operation.hide
import io.element.android.libraries.architecture.overlay.operation.show
import io.element.android.libraries.core.coroutine.CoroutineDispatchers
import io.element.android.libraries.dateformatter.api.DateFormatter
import io.element.android.libraries.dateformatter.api.DateFormatterMode
import io.element.android.libraries.dateformatter.api.toHumanReadableDuration
import io.element.android.libraries.di.RoomScope
import io.element.android.libraries.matrix.api.MatrixClient
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.core.toRoomIdOrAlias
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.permalink.PermalinkData
import io.element.android.libraries.matrix.api.room.BaseRoom
import io.element.android.libraries.matrix.api.room.alias.matches
import io.element.android.libraries.matrix.api.room.joinedRoomMembers
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.matrix.api.timeline.item.TimelineItemDebugInfo
import io.element.android.libraries.matrix.ui.messages.RoomMemberProfilesCache
import io.element.android.libraries.matrix.ui.messages.RoomNamesCache
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.MediaViewerEntryPoint
import io.element.android.libraries.textcomposer.mentions.LocalMentionSpanUpdater
import io.element.android.libraries.textcomposer.mentions.MentionSpanTheme
import io.element.android.libraries.textcomposer.mentions.MentionSpanUpdater
import io.element.android.services.analytics.api.AnalyticsService
import io.element.android.services.analyticsproviders.api.trackers.captureInteraction
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize

@ContributesNode(RoomScope::class)
class MessagesFlowNode @AssistedInject constructor(
    @Assisted buildContext: BuildContext,
    @Assisted plugins: List<Plugin>,
    private val matrixClient: MatrixClient,
    private val sendLocationEntryPoint: SendLocationEntryPoint,
    private val showLocationEntryPoint: ShowLocationEntryPoint,
    private val createPollEntryPoint: CreatePollEntryPoint,
    private val elementCallEntryPoint: ElementCallEntryPoint,
    private val mediaViewerEntryPoint: MediaViewerEntryPoint,
    private val analyticsService: AnalyticsService,
    private val locationService: LocationService,
    private val room: BaseRoom,
    private val roomMemberProfilesCache: RoomMemberProfilesCache,
    private val roomNamesCache: RoomNamesCache,
    private val mentionSpanUpdater: MentionSpanUpdater,
    private val mentionSpanTheme: MentionSpanTheme,
    private val pinnedEventsTimelineProvider: PinnedEventsTimelineProvider,
    private val timelineController: TimelineController,
    private val knockRequestsListEntryPoint: KnockRequestsListEntryPoint,
    private val dateFormatter: DateFormatter,
    private val coroutineDispatchers: CoroutineDispatchers,
) : BaseFlowNode<MessagesFlowNode.NavTarget>(
    backstack = BackStack(
        initialElement = plugins.filterIsInstance<MessagesEntryPoint.Params>().first().initialTarget.toNavTarget(),
        savedStateMap = buildContext.savedStateMap,
    ),
    overlay = Overlay(
        savedStateMap = buildContext.savedStateMap,
    ),
    buildContext = buildContext,
    plugins = plugins
) {
    sealed interface NavTarget : Parcelable {
        @Parcelize
        data object Empty : NavTarget

        @Parcelize
        data class Messages(val focusedEventId: EventId?) : NavTarget

        @Parcelize
        data class MediaViewer(
            val mode: MediaViewerEntryPoint.MediaViewerMode,
            val eventId: EventId?,
            val mediaInfo: MediaInfo,
            val mediaSource: MediaSource,
            val thumbnailSource: MediaSource?,
        ) : NavTarget

        @Parcelize
        data class AttachmentPreview(val attachment: Attachment) : NavTarget

        @Parcelize
        data class LocationViewer(val location: Location, val description: String?) : NavTarget

        @Parcelize
        data class EventDebugInfo(val eventId: EventId?, val debugInfo: TimelineItemDebugInfo) : NavTarget

        @Parcelize
        data class ForwardEvent(val eventId: EventId, val fromPinnedEvents: Boolean) : NavTarget

        @Parcelize
        data class ReportMessage(val eventId: EventId, val senderId: UserId) : NavTarget

        @Parcelize
        data object SendLocation : NavTarget

        @Parcelize
        data object CreatePoll : NavTarget

        @Parcelize
        data class EditPoll(val eventId: EventId) : NavTarget

        @Parcelize
        data object PinnedMessagesList : NavTarget

        @Parcelize
        data object KnockRequestsList : NavTarget
    }

    private val callbacks = plugins<MessagesEntryPoint.Callback>()

    override fun onBuilt() {
        super.onBuilt()
        lifecycle.subscribe(
            onDestroy = {
                timelineController.close()
            }
        )
        setupCacheUpdaters()

        pinnedEventsTimelineProvider.launchIn(lifecycleScope)
    }

    private fun setupCacheUpdaters() {
        room.membersStateFlow
            .onEach { membersState ->
                withContext(coroutineDispatchers.computation) {
                    roomMemberProfilesCache.replace(membersState.joinedRoomMembers())
                }
            }
            .launchIn(lifecycleScope)

        matrixClient.roomListService
            .allRooms
            .summaries
            .onEach {
                withContext(coroutineDispatchers.computation) {
                    roomNamesCache.replace(it)
                }
            }
            .launchIn(lifecycleScope)
    }

    override fun resolve(navTarget: NavTarget, buildContext: BuildContext): Node {
        return when (navTarget) {
            is NavTarget.Messages -> {
                val callback = object : MessagesNode.Callback {
                    override fun onRoomDetailsClick() {
                        callbacks.forEach { it.onRoomDetailsClick() }
                    }

                    override fun onEventClick(isLive: Boolean, event: TimelineItem.Event): Boolean {
                        return processEventClick(
                            timelineMode = if (isLive) Timeline.Mode.LIVE else Timeline.Mode.FOCUSED_ON_EVENT,
                            event = event,
                        )
                    }

                    override fun onPreviewAttachments(attachments: ImmutableList<Attachment>) {
                        backstack.push(NavTarget.AttachmentPreview(attachments.first()))
                    }

                    override fun onUserDataClick(userId: UserId) {
                        callbacks.forEach { it.onUserDataClick(userId) }
                    }

                    override fun onPermalinkClick(data: PermalinkData) {
                        callbacks.forEach { it.onPermalinkClick(data, pushToBackstack = true) }
                    }

                    override fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
                        backstack.push(NavTarget.EventDebugInfo(eventId, debugInfo))
                    }

                    override fun onForwardEventClick(eventId: EventId) {
                        backstack.push(NavTarget.ForwardEvent(eventId, fromPinnedEvents = false))
                    }

                    override fun onReportMessage(eventId: EventId, senderId: UserId) {
                        backstack.push(NavTarget.ReportMessage(eventId, senderId))
                    }

                    override fun onSendLocationClick() {
                        backstack.push(NavTarget.SendLocation)
                    }

                    override fun onCreatePollClick() {
                        backstack.push(NavTarget.CreatePoll)
                    }

                    override fun onEditPollClick(eventId: EventId) {
                        backstack.push(NavTarget.EditPoll(eventId))
                    }

                    override fun onJoinCallClick(roomId: RoomId) {
                        val callType = CallType.RoomCall(
                            sessionId = matrixClient.sessionId,
                            roomId = roomId,
                        )
                        analyticsService.captureInteraction(Interaction.Name.MobileRoomCallButton)
                        elementCallEntryPoint.startCall(callType)
                    }

                    override fun onViewAllPinnedEvents() {
                        backstack.push(NavTarget.PinnedMessagesList)
                    }

                    override fun onViewKnockRequests() {
                        backstack.push(NavTarget.KnockRequestsList)
                    }
                }
                val inputs = MessagesNode.Inputs(focusedEventId = navTarget.focusedEventId)
                createNode<MessagesNode>(buildContext, listOf(callback, inputs))
            }
            is NavTarget.MediaViewer -> {
                val params = MediaViewerEntryPoint.Params(
                    mode = navTarget.mode,
                    eventId = navTarget.eventId,
                    mediaInfo = navTarget.mediaInfo,
                    mediaSource = navTarget.mediaSource,
                    thumbnailSource = navTarget.thumbnailSource,
                    canShowInfo = true,
                )
                val callback = object : MediaViewerEntryPoint.Callback {
                    override fun onDone() {
                        overlay.hide()
                    }

                    override fun onViewInTimeline(eventId: EventId) {
                        viewInTimeline(eventId)
                    }
                }
                mediaViewerEntryPoint.nodeBuilder(this, buildContext)
                    .params(params)
                    .callback(callback)
                    .build()
            }
            is NavTarget.AttachmentPreview -> {
                val inputs = AttachmentsPreviewNode.Inputs(navTarget.attachment)
                createNode<AttachmentsPreviewNode>(buildContext, listOf(inputs))
            }
            is NavTarget.LocationViewer -> {
                val inputs = ShowLocationEntryPoint.Inputs(navTarget.location, navTarget.description)
                showLocationEntryPoint.createNode(this, buildContext, inputs)
            }
            is NavTarget.EventDebugInfo -> {
                val inputs = EventDebugInfoNode.Inputs(navTarget.eventId, navTarget.debugInfo)
                createNode<EventDebugInfoNode>(buildContext, listOf(inputs))
            }
            is NavTarget.ForwardEvent -> {
                val timelineProvider = if (navTarget.fromPinnedEvents) {
                    pinnedEventsTimelineProvider
                } else {
                    timelineController
                }
                val inputs = ForwardMessagesNode.Inputs(navTarget.eventId, timelineProvider)
                val callback = object : ForwardMessagesNode.Callback {
                    override fun onForwardedToSingleRoom(roomId: RoomId) {
                        callbacks.forEach { it.onForwardedToSingleRoom(roomId) }
                    }
                }
                createNode<ForwardMessagesNode>(buildContext, listOf(inputs, callback))
            }
            is NavTarget.ReportMessage -> {
                val inputs = ReportMessageNode.Inputs(navTarget.eventId, navTarget.senderId)
                createNode<ReportMessageNode>(buildContext, listOf(inputs))
            }
            NavTarget.SendLocation -> {
                sendLocationEntryPoint.createNode(this, buildContext)
            }
            NavTarget.CreatePoll -> {
                createPollEntryPoint.nodeBuilder(this, buildContext)
                    .params(CreatePollEntryPoint.Params(mode = CreatePollMode.NewPoll))
                    .build()
            }
            is NavTarget.EditPoll -> {
                createPollEntryPoint.nodeBuilder(this, buildContext)
                    .params(CreatePollEntryPoint.Params(mode = CreatePollMode.EditPoll(eventId = navTarget.eventId)))
                    .build()
            }
            NavTarget.PinnedMessagesList -> {
                val callback = object : PinnedMessagesListNode.Callback {
                    override fun onEventClick(event: TimelineItem.Event) {
                        processEventClick(
                            timelineMode = Timeline.Mode.PINNED_EVENTS,
                            event = event,
                        )
                    }

                    override fun onUserDataClick(userId: UserId) {
                        callbacks.forEach { it.onUserDataClick(userId) }
                    }

                    override fun onViewInTimelineClick(eventId: EventId) {
                        viewInTimeline(eventId)
                    }

                    override fun onRoomPermalinkClick(data: PermalinkData.RoomLink) {
                        callbacks.forEach { it.onPermalinkClick(data, pushToBackstack = !room.matches(data.roomIdOrAlias)) }
                    }

                    override fun onShowEventDebugInfoClick(eventId: EventId?, debugInfo: TimelineItemDebugInfo) {
                        backstack.push(NavTarget.EventDebugInfo(eventId, debugInfo))
                    }

                    override fun onForwardEventClick(eventId: EventId) {
                        backstack.push(NavTarget.ForwardEvent(eventId = eventId, fromPinnedEvents = true))
                    }
                }
                createNode<PinnedMessagesListNode>(buildContext, plugins = listOf(callback))
            }
            NavTarget.Empty -> {
                node(buildContext) {}
            }
            NavTarget.KnockRequestsList -> {
                knockRequestsListEntryPoint.createNode(this, buildContext)
            }
        }
    }

    private fun viewInTimeline(eventId: EventId) {
        val permalinkData = PermalinkData.RoomLink(
            roomIdOrAlias = room.roomId.toRoomIdOrAlias(),
            eventId = eventId,
        )
        callbacks.forEach { it.onPermalinkClick(permalinkData, pushToBackstack = false) }
    }

    private fun processEventClick(
        timelineMode: Timeline.Mode,
        event: TimelineItem.Event,
    ): Boolean {
        val navTarget = when (event.content) {
            is TimelineItemImageContent -> {
                buildMediaViewerNavTarget(
                    mode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(timelineMode),
                    event = event,
                    content = event.content,
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                )
            }
            is TimelineItemVideoContent -> {
                buildMediaViewerNavTarget(
                    mode = MediaViewerEntryPoint.MediaViewerMode.TimelineImagesAndVideos(timelineMode),
                    event = event,
                    content = event.content,
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                )
            }
            is TimelineItemFileContent -> {
                buildMediaViewerNavTarget(
                    mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode),
                    event = event,
                    content = event.content,
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = event.content.thumbnailSource,
                )
            }
            is TimelineItemAudioContent -> {
                buildMediaViewerNavTarget(
                    mode = MediaViewerEntryPoint.MediaViewerMode.TimelineFilesAndAudios(timelineMode),
                    event = event,
                    content = event.content,
                    mediaSource = event.content.mediaSource,
                    thumbnailSource = null,
                )
            }
            is TimelineItemLocationContent -> {
                NavTarget.LocationViewer(
                    location = event.content.location,
                    description = event.content.description,
                ).takeIf { locationService.isServiceAvailable() }
            }
            else -> null
        }
        return when (navTarget) {
            is NavTarget.MediaViewer -> {
                overlay.show(navTarget)
                true
            }
            is NavTarget.LocationViewer -> {
                backstack.push(navTarget)
                true
            }
            else -> false
        }
    }

    private fun buildMediaViewerNavTarget(
        mode: MediaViewerEntryPoint.MediaViewerMode,
        event: TimelineItem.Event,
        content: TimelineItemEventContentWithAttachment,
        mediaSource: MediaSource,
        thumbnailSource: MediaSource?,
    ): NavTarget {
        return NavTarget.MediaViewer(
            mode = mode,
            eventId = event.eventId,
            mediaInfo = MediaInfo(
                filename = content.filename,
                caption = content.caption,
                mimeType = content.mimeType,
                formattedFileSize = content.formattedFileSize,
                fileExtension = content.fileExtension,
                senderId = event.senderId,
                senderName = event.safeSenderName,
                senderAvatar = event.senderAvatar.url,
                dateSent = dateFormatter.format(
                    event.sentTimeMillis,
                    mode = DateFormatterMode.Day,
                ),
                dateSentFull = dateFormatter.format(
                    timestamp = event.sentTimeMillis,
                    mode = DateFormatterMode.Full,
                ),
                waveform = (content as? TimelineItemVoiceContent)?.waveform,
                duration = content.duration()?.toHumanReadableDuration(),
            ),
            mediaSource = mediaSource,
            thumbnailSource = thumbnailSource,
        )
    }

    @Composable
    override fun View(modifier: Modifier) {
        mentionSpanTheme.updateStyles()
        CompositionLocalProvider(
            LocalMentionSpanUpdater provides mentionSpanUpdater
        ) {
            BackstackWithOverlayBox(modifier)
        }
    }
}
