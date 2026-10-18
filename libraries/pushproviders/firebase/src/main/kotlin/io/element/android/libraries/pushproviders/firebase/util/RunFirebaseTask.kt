/*
 * Copyright (c) 2026 Element Creations Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
 * Please see LICENSE files in the repository root for full details.
 */

package io.element.android.libraries.pushproviders.firebase.util

import com.google.android.gms.tasks.Task
import io.element.android.libraries.core.extensions.runCatchingExceptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> runFirebaseTaskWithResult(closure: () -> Task<T>): Result<T> {
    return runCatchingExceptions {
        suspendCancellableCoroutine { continuation ->
            try {
                closure().addOnSuccessListener {
                    continuation.resume(it)
                }.addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
            } catch (e: Throwable) {
                continuation.resumeWithException(e)
            }
        }
    }
}

// Special case for `Void!` return type, which is not a valid Kotlin type. We convert it to `Unit` instead.
suspend fun runFirebaseTask(closure: () -> Task<Void?>): Result<Unit> {
    return runFirebaseTaskWithResult<Unit> { closure().continueWith {} }
}
