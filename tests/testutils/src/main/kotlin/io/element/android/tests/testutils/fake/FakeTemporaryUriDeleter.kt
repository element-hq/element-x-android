/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.tests.testutils.fake

import android.net.Uri
import io.element.android.libraries.androidutils.file.TemporaryUriDeleter
import io.element.android.tests.testutils.lambda.lambdaError

class FakeTemporaryUriDeleter(
    val deleteLambda: (uri: Uri?) -> Unit = { lambdaError() }
) : TemporaryUriDeleter {
    override fun delete(uri: Uri?) {
        deleteLambda(uri)
    }
}
