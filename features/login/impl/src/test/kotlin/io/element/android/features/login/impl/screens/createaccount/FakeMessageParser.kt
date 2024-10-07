/*
 * Copyright 2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package io.element.android.features.login.impl.screens.createaccount

import io.element.android.libraries.matrix.api.auth.external.ExternalSession
import io.element.android.tests.testutils.lambda.lambdaError

class FakeMessageParser(
    private val parseResult: (String) -> ExternalSession = { lambdaError() }
) : MessageParser {
    override fun parse(message: String): ExternalSession {
        return parseResult(message)
    }
}
