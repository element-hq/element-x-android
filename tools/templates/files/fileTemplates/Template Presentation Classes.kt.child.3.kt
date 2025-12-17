#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}#end

// TODO add your ui models. Remove the eventSink if you don't have events.
data class ${NAME}State(
    val eventSink: (${NAME}Event) -> Unit
)
