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

package io.element.android.tests.uitests

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.test.isRoot
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onLast
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.airbnb.android.showkase.models.Showkase
import com.github.takahirom.roborazzi.DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.google.testing.junit.testparameterinjector.TestParameter
import io.element.android.features.leaveroom.api.LeaveRoomState
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsState
import io.element.android.features.roomdetails.impl.members.details.RoomMemberDetailsView
import io.element.android.features.roomdetails.impl.members.details.aRoomMemberDetailsState
import io.element.android.features.roomlist.impl.InvitesState
import io.element.android.features.roomlist.impl.RoomListState
import io.element.android.features.roomlist.impl.RoomListView
import io.element.android.features.roomlist.impl.model.RoomListRoomSummary
import io.element.android.features.roomlist.impl.model.RoomListRoomSummaryPlaceholders
import io.element.android.libraries.designsystem.components.avatar.AvatarData
import io.element.android.libraries.designsystem.theme.ElementTheme
import io.element.android.libraries.matrix.api.core.RoomId
import io.element.android.libraries.matrix.api.core.UserId
import io.element.android.libraries.matrix.api.user.MatrixUser
import io.element.android.tests.uitests.test.TestActivity
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import java.io.File

/**
 * BMA: Inspired from https://github.com/airbnb/Showkase/blob/master/showkase-screenshot-testing-paparazzi-sample/src/test/java/com/airbnb/android/showkase/screenshot/testing/paparazzi/sample/PaparazziSampleScreenshotTest.kt
 */

/*
 * Credit to Alex Vanyo for creating this sample in the Now In Android app by Google.
 * PR here - https://github.com/android/nowinandroid/pull/101. Modified the test from that PR to
 * my own needs for this sample.
 */
//@RunWith(TestParameterInjector::class)
@Config(sdk = [30], qualifiers = RobolectricDeviceQualifiers.Pixel5)
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotDumpTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<TestActivity>()

    @Test
    fun preview_tests() {
        val scenario = ActivityScenario.launch(TestActivity::class.java)
        composeTestRule.setContent {
            CompositionLocalProvider(
                LocalInspectionMode provides true,
            ) {
                ElementTheme {
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .sizeIn(minWidth = 1.dp, minHeight = 1.dp, maxHeight = 1000.dp)
                    ) {
                        val state = aRoomMemberDetailsState().copy(displayConfirmationDialog = RoomMemberDetailsState.ConfirmationDialog.Block)
                        RoomMemberDetailsView(state = state, onShareUser = { /*TODO*/ }, goBack = { /*TODO*/ })
                    }
                }
            }
        }
        val onAllRootNodes = composeTestRule.onAllNodes(isRoot())
        val allNodes = onAllRootNodes.fetchSemanticsNodes()

        allNodes.forEachIndexed { index, node -> println("Node #$index: ${node.boundsInWindow}") }
        onAllRootNodes.onFirst().captureRoboImage(
            File(DEFAULT_ROBORAZZI_OUTPUT_DIR_PATH, "roborazzi_dump.png"),
            RoborazziOptions(captureType = RoborazziOptions.CaptureType.Dump())
        )
        scenario.close()
    }
}
