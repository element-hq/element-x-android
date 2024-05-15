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

package io.element.android.libraries.testtags

@JvmInline
value class TestTag internal constructor(val value: String)

object TestTags {
    /**
     * OnBoarding screen.
     */
    val onBoardingSignIn = TestTag("onboarding-sign_in")

    /**
     * Login screen.
     */
    val loginChangeServer = TestTag("login-change_server")
    val loginEmailUsername = TestTag("login-email_username")
    val loginPassword = TestTag("login-password")
    val loginContinue = TestTag("login-continue")

    /**
     * Verification screen.
     */
    val recoveryKey = TestTag("verification-recovery_key")

    /**
     * Sign out screen.
     */
    val signOut = TestTag("sign-out-submit")

    /**
     * Change server screen.
     */
    val changeServerServer = TestTag("change_server-server")

    /**
     * Room list / Home screen.
     */
    val homeScreenSettings = TestTag("home_screen-settings")
    val homeScreenClearFilters = TestTag("home_screen-clear_filters")

    /**
     * Room detail screen.
     */
    val roomDetailAvatar = TestTag("room_detail-avatar")

    /**
     * Room member screen.
     */
    val memberDetailAvatar = TestTag("member_detail-avatar")

    /**
     * Edit avatar.
     */
    val editAvatar = TestTag("edit-avatar")

    /**
     * Welcome screen.
     */
    val welcomeScreenTitle = TestTag("welcome_screen-title")

    /**
     * RichTextEditor.
     */
    val richTextEditor = TestTag("rich_text_editor")

    /**
     * Message bubble.
     */
    val messageBubble = TestTag("message_bubble")

    /**
     * Message Read Receipts.
     */
    val messageReadReceipts = TestTag("message_read_receipts")

    /**
     * Dialogs.
     */
    val dialogPositive = TestTag("dialog-positive")
    val dialogNegative = TestTag("dialog-negative")
    val dialogNeutral = TestTag("dialog-neutral")

    /**
     * Floating Action Button.
     */
    val floatingActionButton = TestTag("floating-action-button")

    /**
     * Timeline item.
     */
    val timelineItemSenderInfo = TestTag("timeline_item-sender_info")

    /**
     * Search field.
     */
    val searchTextField = TestTag("search_text_field")

    /**
     * Generic call to action.
     */
    val callToAction = TestTag("call_to_action")
}
