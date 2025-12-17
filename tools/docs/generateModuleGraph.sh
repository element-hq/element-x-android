#!/usr/bin/env bash

# Copyright (c) 2025 Element Creations Ltd.
# Copyright 2022-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-Element-Commercial.
# Please see LICENSE files in the repository root for full details.

## Dependency graph https://github.com/savvasdalkitsis/module-dependency-graph
dotPath=$(pwd)/docs/images/module_graph.dot
pngPath=$(pwd)/docs/images/module_graph.png
./gradlew graphModules -PdotFilePath="${dotPath}" -PgraphOutputFilePath="${pngPath}" -PautoOpenGraph=false
rm "${dotPath}"
