/*
 * Copyright (c) 2022 New Vector Ltd
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

package extension

import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.IOException

private fun Project.runCommand(cmd: String): String {
    val outputStream = ByteArrayOutputStream()
    val errorStream = ByteArrayOutputStream()
    project.exec {
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

fun Project.gitRevision() = runCommand("git rev-parse --short=8 HEAD")

fun Project.gitBranchName() = runCommand("git rev-parse --abbrev-ref HEAD")
