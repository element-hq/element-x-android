#!/usr/bin/env bash

# Copyright 2022-2024 New Vector Ltd.
#
# SPDX-License-Identifier: AGPL-3.0-only
# Please see LICENSE in the repository root for full details.

## Dependency graph https://github.com/savvasdalkitsis/module-dependency-graph
dotPath=$(pwd)/docs/images/module_graph.dot
pngPath=$(pwd)/docs/images/module_graph.png
./gradlew graphModules -PdotFilePath="${dotPath}" -PgraphOutputFilePath="${pngPath}" -PautoOpenGraph=false
rm "${dotPath}"
