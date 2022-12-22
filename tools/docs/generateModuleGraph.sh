#!/usr/bin/env bash

## Dependency graph https://github.com/savvasdalkitsis/module-dependency-graph
dotPath=`pwd`/docs/images/module_graph.dot
pngPath=`pwd`/docs/images/module_graph.png
./gradlew graphModules -PdotFilePath=${dotPath} -PgraphOutputFilePath=${pngPath} -PautoOpenGraph=false
rm ${dotPath}
