/*
 * Copyright 2023, 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package io.element.android.features.messages.impl

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.element.android.compound.theme.ElementTheme
import io.element.android.features.maprealtime.impl.MapRealtimePresenterState
import io.element.android.features.maprealtime.impl.MapRealtimeView
import io.element.android.features.messages.impl.actionlist.model.TimelineItemAction
import io.element.android.features.messages.impl.timeline.components.receipt.bottomsheet.ReadReceiptBottomSheetEvents
import io.element.android.features.messages.impl.timeline.model.TimelineItem
import io.element.android.features.networkmonitor.api.ui.ConnectivityIndicatorView
import io.element.android.libraries.androidutils.ui.hideKeyboard
import io.element.android.libraries.designsystem.preview.ElementPreview
import io.element.android.libraries.designsystem.preview.PreviewsDayNight
import io.element.android.libraries.designsystem.theme.components.Scaffold
import io.element.android.libraries.designsystem.utils.snackbar.SnackbarHost
import io.element.android.libraries.designsystem.utils.snackbar.rememberSnackbarHostState
import io.element.android.libraries.matrix.api.core.UserId

private const val BOTTOM_SHEET_CONTENT_TRANSITION_DURATION_IN_MS = 300
private const val SHEET_PEEK_HEIGHT_DIVISOR = 4
private const val MAX_HEIGHT_DIVISOR = 2

@Composable
fun MessagesView(
    state: MessagesState,
    mapRealtimeState: MapRealtimePresenterState,
    onBackClick: () -> Unit,
    onRoomDetailsClick: () -> Unit,
    onEventContentClick: (event: TimelineItem.Event) -> Boolean,
    onUserDataClick: (UserId) -> Unit,
    onLinkClick: (String) -> Unit,
    onSendLocationClick: () -> Unit,
    onCreatePollClick: () -> Unit,
    onJoinCallClick: () -> Unit,
    onViewAllPinnedMessagesClick: () -> Unit,
    modifier: Modifier = Modifier,
    forceJumpToBottomVisibility: Boolean = false,
    knockRequestsBannerView: @Composable () -> Unit,
) {
    val snackbarHostState = rememberSnackbarHostState(snackbarMessage = state.snackbarMessage)

    val localView = LocalView.current

    fun hidingKeyboard(block: () -> Unit) {
        localView.hideKeyboard()
        block()
    }

    fun localOnBackClick() {
        // Since the textfield is now based on an Android view, this is no longer done automatically.
        // We need to hide the keyboard when navigating out of this screen.
        localView.hideKeyboard()
        onBackClick()
    }

    @Composable
    fun MessagesViewContent(
        modifier: Modifier,
        timelineLazyListState: LazyListState = rememberLazyListState(),
    ) {
        MessagesViewContent(
            state = state,
            modifier = modifier,
            onUserDataClick = { hidingKeyboard { onUserDataClick(it) } },
            onLinkClick = onLinkClick,
            onReadReceiptClick = { event ->
                state.readReceiptBottomSheetState.eventSink(ReadReceiptBottomSheetEvents.EventSelected(event))
            },
            onEventContentClick = onEventContentClick,
            onSendLocationClick = onSendLocationClick,
            onCreatePollClick = onCreatePollClick,
            onSwipeToReply = { targetEvent ->
                state.eventSink(MessagesEvents.HandleAction(TimelineItemAction.Reply, targetEvent))
            },
            forceJumpToBottomVisibility = forceJumpToBottomVisibility,
            onJoinCallClick = onJoinCallClick,
            onViewAllPinnedMessagesClick = onViewAllPinnedMessagesClick,
            timelineLazyListState = timelineLazyListState,
            knockRequestsBannerView = knockRequestsBannerView,
        )
    }

    if (!state.showMapView) {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets.statusBars,
            topBar = {
                Column {
                    ConnectivityIndicatorView(isOnline = state.hasNetworkConnection)
                    MessagesViewTopBar(
                        roomName = state.roomName.dataOrNull(),
                        roomAvatar = state.roomAvatar.dataOrNull(),
                        heroes = state.heroes,
                        roomCallState = state.roomCallState,
                        onBackClick = ::localOnBackClick,
                        onRoomDetailsClick = onRoomDetailsClick,
                        onJoinCallClick = onJoinCallClick,
                        onShowMapClick = {
                            state.eventSink(MessagesEvents.ShowMap)
                        }
                    )
                }
            },
            content = { padding ->
                MessagesViewContent(
                    modifier = Modifier
                        .padding(padding)
                        .consumeWindowInsets(padding),
                )
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.navigationBarsPadding(),
                )
            },
        )
    } else {
        val scaffoldState = rememberBottomSheetScaffoldState(
            bottomSheetState = rememberStandardBottomSheetState(
                skipHiddenState = true
            ),
            snackbarHostState = snackbarHostState,
        )

        val configuration = LocalConfiguration.current
        val screenHeight: Dp = configuration.screenHeightDp.dp

        val imeInsets = WindowInsets.ime.only(WindowInsetsSides.Bottom).asPaddingValues()
        val imeBottomPadding = imeInsets.calculateBottomPadding()

        val sheetPeekHeight: Dp = (screenHeight / SHEET_PEEK_HEIGHT_DIVISOR) + imeBottomPadding
        val maxHeight: Dp = (screenHeight / MAX_HEIGHT_DIVISOR) + imeBottomPadding

        val timelineLazyListState: LazyListState = rememberLazyListState()

        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = sheetPeekHeight,
            sheetSwipeEnabled = !timelineLazyListState.isScrollInProgress,
            sheetContainerColor = ElementTheme.colors.bgCanvasDefault,
            sheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            sheetContent = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(maxHeight)
                ) {
                    val navBarInsets = WindowInsets.navigationBars.only(WindowInsetsSides.Bottom)
                    val navBarBottomPadding = navBarInsets.asPaddingValues().calculateBottomPadding()

                    val animatedBottomPadding: Dp by animateDpAsState(
                        targetValue = if (scaffoldState.bottomSheetState.currentValue == SheetValue.PartiallyExpanded) {
                            maxHeight - sheetPeekHeight + navBarBottomPadding + 24.dp
                        } else {
                            0.dp
                        },
                        animationSpec = tween(durationMillis = BOTTOM_SHEET_CONTENT_TRANSITION_DURATION_IN_MS),
                        label = "bottomSheetContentPaddingAnimation"
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(maxHeight)
                            .padding(bottom = animatedBottomPadding),
                        verticalArrangement = Arrangement.Bottom,
                    ) {
                        MessagesViewContent(
                            modifier = Modifier,
                            timelineLazyListState = timelineLazyListState,
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.navigationBarsPadding(),
                )
            },
        ) { _ ->
            MapRealtimeView(
                state = mapRealtimeState,
                onBackPressed = ::localOnBackClick,
                onJoinCallClick = onJoinCallClick,
                roomCallState = state.roomCallState,
                onMessagesPressed = {
                    state.eventSink(MessagesEvents.HideMap)
                })
        }
    }
}

@Composable
fun keyboardAsState(): State<Boolean> {
    val isImeVisible = WindowInsets.ime.getBottom(LocalDensity.current) > 0
    return rememberUpdatedState(newValue = isImeVisible)
}

@PreviewsDayNight
@Composable
internal fun MessagesViewPreview(@PreviewParameter(MessagesStateProvider::class) state: MessagesState) = ElementPreview {
    MessagesView(
        state = state,
        onBackClick = {},
        onRoomDetailsClick = {},
        onEventContentClick = { false },
        onUserDataClick = {},
        onLinkClick = {},
        onSendLocationClick = {},
        onCreatePollClick = {},
        onJoinCallClick = {},
        onViewAllPinnedMessagesClick = { },
        forceJumpToBottomVisibility = true,
        knockRequestsBannerView = {},
        mapRealtimeState = FakeMapRealtimeStateProvider().values.first(),
    )
}
