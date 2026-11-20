/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.features.callnative.api

import io.element.android.call.ui.ElementCallActivity

/**
 * Element X's half of the native call screen.
 *
 * The full-screen call is the library's own `ElementCallActivity`, which looks up its host on the
 * `Application`: that is the only place it can find one when it is started over the lock screen with
 * nothing of the app behind it. `ElementXApplication` cannot carry the call stack itself, so it
 * implements [ElementCallActivity.Host] by forwarding every member here.
 */
interface NativeCallScreenHost : ElementCallActivity.Host
