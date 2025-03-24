/*
 * Copyright 2025 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.messages.impl.link

import io.element.android.libraries.architecture.AsyncAction
import io.element.android.wysiwyg.link.Link

data class ConfirmingLinkClick(
    val link: Link,
) : AsyncAction.Confirming
