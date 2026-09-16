/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.matrix.api

import java.lang.AutoCloseable

/**
 * A Matrix client in an 'unauthenticated' state that can still be used to resolve URLs.
 *
 * Note: this client implements an [AutoCloseable] interface, so using [AutoCloseable.close] or [AutoCloseable.use] will clean up any resources associated
 * with it, such as temporary files.
 */
interface TemporaryMatrixClient : ClientUrlContentFetcher, AutoCloseable
