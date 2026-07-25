/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.tests.testutils.robolectric

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Base class for all Robolectric tests.
 *
 * It is now configured to run by default on Android API 36,
 * waiting for Robolectric to support API 37.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
open class RobolectricTest
