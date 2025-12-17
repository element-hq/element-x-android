/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.mediaviewer.impl.viewer

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import io.element.android.libraries.architecture.AsyncData
import io.element.android.libraries.designsystem.components.media.WaveFormSamples
import io.element.android.libraries.matrix.api.core.EventId
import io.element.android.libraries.matrix.api.media.MediaSource
import io.element.android.libraries.matrix.api.timeline.Timeline
import io.element.android.libraries.mediaviewer.api.MediaInfo
import io.element.android.libraries.mediaviewer.api.aPdfMediaInfo
import io.element.android.libraries.mediaviewer.api.aTxtMediaInfo
import io.element.android.libraries.mediaviewer.api.aVideoMediaInfo
import io.element.android.libraries.mediaviewer.api.anApkMediaInfo
import io.element.android.libraries.mediaviewer.api.anAudioMediaInfo
import io.element.android.libraries.mediaviewer.api.anImageMediaInfo
import io.element.android.libraries.mediaviewer.api.local.LocalMedia
import io.element.android.libraries.mediaviewer.impl.details.MediaBottomSheetState
import io.element.android.libraries.mediaviewer.impl.details.aMediaDeleteConfirmationState
import io.element.android.libraries.mediaviewer.impl.details.aMediaDetailsBottomSheetState
import kotlinx.collections.immutable.toImmutableList

open class MediaViewerStateProvider : PreviewParameterProvider<MediaViewerState> {
    override val values: Sequence<MediaViewerState>
        get() = sequenceOf(
            aMediaViewerState(),
            aMediaViewerState(listOf(aMediaViewerPageData(AsyncData.Loading()))),
            aMediaViewerState(listOf(aMediaViewerPageData(AsyncData.Failure(IllegalStateException("error"))))),
            anImageMediaInfo(
                senderName = "Sally Sanderson",
                dateSent = "21 NOV, 2024",
                caption = "A caption",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aVideoMediaInfo(
                senderName = "A very long name so that it will be truncated and will not be displayed on multiple lines",
                dateSent = "A very very long date that will be truncated and will not be displayed on multiple lines",
                caption = "A caption",
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aPdfMediaInfo().let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aMediaViewerState(
                listOf(
                    aMediaViewerPageData(
                        downloadedMedia = AsyncData.Loading(),
                        mediaInfo = anApkMediaInfo(),
                    )
                )
            ),
            anApkMediaInfo().let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aMediaViewerState(
                listOf(
                    aMediaViewerPageData(
                        downloadedMedia = AsyncData.Loading(),
                        mediaInfo = anAudioMediaInfo(),
                    )
                )
            ),
            anAudioMediaInfo().let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            anImageMediaInfo().let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    ),
                    canShowInfo = false,
                )
            },
            aMediaViewerState(
                mediaBottomSheetState = aMediaDetailsBottomSheetState(),
            ),
            aMediaViewerState(
                mediaBottomSheetState = aMediaDeleteConfirmationState(),
            ),
            anAudioMediaInfo(
                waveForm = WaveFormSamples.realisticWaveForm,
            ).let {
                aMediaViewerState(
                    listOf(
                        aMediaViewerPageData(
                            downloadedMedia = AsyncData.Success(
                                LocalMedia(Uri.EMPTY, it)
                            ),
                            mediaInfo = it,
                        )
                    )
                )
            },
            aMediaViewerState(
                listOf(
                    aMediaViewerPageDataLoading()
                ),
            ),
            aMediaViewerState(
                listOf(
                    MediaViewerPageData.Failure(Exception("error"))
                ),
            ),
            aMediaViewerState(
                listOf(
                    aMediaViewerPageData(
                        downloadedMedia = AsyncData.Loading(),
                        mediaInfo = aTxtMediaInfo(),
                    )
                )
            ),
        )
}

fun aMediaViewerPageDataLoading(
    direction: Timeline.PaginationDirection = Timeline.PaginationDirection.BACKWARDS,
    timestamp: Long = 0L,
): MediaViewerPageData {
    return MediaViewerPageData.Loading(
        direction = direction,
        timestamp = timestamp,
        pagerKey = 0L,
    )
}

fun aMediaViewerPageData(
    downloadedMedia: AsyncData<LocalMedia> = AsyncData.Uninitialized,
    mediaInfo: MediaInfo = anImageMediaInfo(),
    mediaSource: MediaSource = MediaSource(""),
): MediaViewerPageData.MediaViewerData = MediaViewerPageData.MediaViewerData(
    eventId = null,
    mediaInfo = mediaInfo,
    mediaSource = mediaSource,
    thumbnailSource = null,
    downloadedMedia = mutableStateOf(downloadedMedia),
    pagerKey = 0L,
)

fun aMediaViewerState(
    listData: List<MediaViewerPageData> = listOf(aMediaViewerPageData()),
    currentIndex: Int = 0,
    canShowInfo: Boolean = true,
    mediaBottomSheetState: MediaBottomSheetState = MediaBottomSheetState.Hidden,
    eventSink: (MediaViewerEvents) -> Unit = {},
) = MediaViewerState(
    initiallySelectedEventId = EventId("\$a:b"),
    listData = listData.toImmutableList(),
    currentIndex = currentIndex,
    snackbarMessage = null,
    canShowInfo = canShowInfo,
    mediaBottomSheetState = mediaBottomSheetState,
    eventSink = eventSink,
)
