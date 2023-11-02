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

    /**
     * Welcome screen.
     */
    val welcomeScreenTitle = TestTag("welcome_screen-title")

    /**
     * RichTextEditor.
     */
    val richTextEditor = TestTag("rich_text_editor")

    /**
     * Dialogs.
     */
    val dialogPositive = TestTag("dialog-positive")
    val dialogNegative = TestTag("dialog-negative")
    val dialogNeutral = TestTag("dialog-neutral")
}


