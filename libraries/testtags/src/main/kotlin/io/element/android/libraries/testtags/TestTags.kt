/*
 * Copyright (c) 2025 Element Creations Ltd.
 * Copyright 2023-2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
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
     * TextEditor.
     */
    val textEditor = TestTag("text_editor")

    /**
     * EditText inside the MarkdownTextInput.
     */
    val plainTextEditor = TestTag("plain_text_editor")

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
     * Timeline.
     */
    val timeline = TestTag("timeline")

    /**
     * Timeline item.
     */
    val timelineItemSenderAvatar = TestTag("timeline_item-sender_avatar")
    val timelineItemSenderName = TestTag("timeline_item-sender_name")

    /**
     * Search field.
     */
    val searchTextField = TestTag("search_text_field")

    /**
     * Generic call to action.
     */
    val callToAction = TestTag("call_to_action")

    /**
     * Room address field.
     *
     */
    val roomAddressField = TestTag("room_address_field")
}
