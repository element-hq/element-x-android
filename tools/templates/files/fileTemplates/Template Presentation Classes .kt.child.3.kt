#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME}#end

// TODO add your ui models. Remove the eventSink if you don't have events.
// Do not use default value, so no member get forgotten in the presenters.
data class ${NAME}State(
    val eventSink: (${NAME}Events) -> Unit
)
