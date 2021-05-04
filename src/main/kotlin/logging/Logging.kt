package logging

import logging.LogLevel.INFO
import logging.LogLevel.TRACE

var rootLogLevel: LogLevel = INFO

enum class LogLevel(val level: Int) {
    TRACE(1),
    DEBUG(2),
    INFO(3),
    NONE(4),
    TEST(5),
}
// TODO: deal with the O non-sense T.() -> String could do the trick?
inline fun <T> T.printO(logLevel: LogLevel = TRACE, messageCreator: (T) -> String): T {
    if (logLevel >= rootLogLevel) println(messageCreator(this))
    return this
}

inline fun <T> T.print(logLevel: LogLevel = TRACE, messageCreator: (String) -> String): T {
    if (logLevel >= rootLogLevel) println(messageCreator(this.toString()))
    return this
}

fun <T> T.print(logLevel: LogLevel = TRACE): T {
    if (logLevel >= rootLogLevel) println(this)
    return this
}