/*
 * Copyright 2022-2024 New Vector Ltd.
 *
 * SPDX-License-Identifier: AGPL-3.0-only
 * Please see LICENSE in the repository root for full details.
 */

package extension

import org.gradle.api.Project
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.process.ExecOperations
import java.io.ByteArrayOutputStream
import java.io.IOException
import javax.inject.Inject

abstract class GitRevisionValueSource : ValueSource<String, ValueSourceParameters.None> {
    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String? {
        return execOperations.runCommand("git rev-parse --short=8 HEAD")
    }
}

abstract class GitBranchNameValueSource : ValueSource<String, ValueSourceParameters.None> {
    @get:Inject
    abstract val execOperations: ExecOperations

    override fun obtain(): String? {
        return execOperations.runCommand("git rev-parse --abbrev-ref HEAD")
    }
}

private fun ExecOperations.runCommand(cmd: String): String {
    val outputStream = ByteArrayOutputStream()
    val errorStream = ByteArrayOutputStream()
    exec {
        commandLine = cmd.split(" ")
        standardOutput = outputStream
        errorOutput = errorStream
    }
    if (errorStream.size() > 0) {
        println("Error while running command: $cmd")
        throw IOException(String(errorStream.toByteArray()))
    }
    return String(outputStream.toByteArray()).trim()
}
