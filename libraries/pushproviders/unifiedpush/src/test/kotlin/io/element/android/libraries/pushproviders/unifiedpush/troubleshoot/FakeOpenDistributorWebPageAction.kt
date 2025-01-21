/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.unifiedpush.troubleshoot

class FakeOpenDistributorWebPageAction(
    private val executeAction: () -> Unit = {}
) : OpenDistributorWebPageAction {
    override fun execute() = executeAction()
}
