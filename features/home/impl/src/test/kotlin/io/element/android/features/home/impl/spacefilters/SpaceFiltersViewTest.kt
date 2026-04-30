/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

@file:OptIn(ExperimentalTestApi::class)

package io.element.android.features.home.impl.spacefilters

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.AndroidComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.v2.runAndroidComposeUiTest
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.element.android.libraries.matrix.test.A_ROOM_ALIAS
import io.element.android.tests.testutils.EventsRecorder
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SpaceFiltersViewTest {
    @Test
    fun `clicking on a filter with alias shows display name and alias`() = runAndroidComposeUiTest {
        val filter = aSpaceServiceFilter(
            displayName = "Test Space",
            canonicalAlias = A_ROOM_ALIAS,
        )
        val eventsRecorder = EventsRecorder<SpaceFiltersEvent.Selecting>()
        setSpaceFiltersView(
            state = aSelectingSpaceFiltersState(
                availableFilters = listOf(filter),
                eventSink = eventsRecorder,
            )
        )

        // Both display name and alias should be visible
        onNodeWithText(filter.spaceRoom.displayName).assertExists()
        onNodeWithText(A_ROOM_ALIAS.value).assertExists()

        onNodeWithText(filter.spaceRoom.displayName).performClick()

        eventsRecorder.assertSingle(SpaceFiltersEvent.Selecting.SelectFilter(filter))
    }

    @Test
    fun `multiple filters are displayed and clickable`() = runAndroidComposeUiTest {
        val filter1 = aSpaceServiceFilter(displayName = "Space One")
        val filter2 = aSpaceServiceFilter(displayName = "Space Two")
        val eventsRecorder = EventsRecorder<SpaceFiltersEvent.Selecting>()
        setSpaceFiltersView(
            state = aSelectingSpaceFiltersState(
                availableFilters = listOf(filter1, filter2),
                eventSink = eventsRecorder,
            )
        )

        // Both filters should be visible
        onNodeWithText(filter1.spaceRoom.displayName).assertExists()
        onNodeWithText(filter2.spaceRoom.displayName).assertExists()

        // Click on second filter
        onNodeWithText(filter2.spaceRoom.displayName).performClick()

        eventsRecorder.assertSingle(SpaceFiltersEvent.Selecting.SelectFilter(filter2))
    }
}

private fun AndroidComposeUiTest<ComponentActivity>.setSpaceFiltersView(
    state: SpaceFiltersState,
) {
    setContent {
        SpaceFiltersView(state = state)
    }
}
