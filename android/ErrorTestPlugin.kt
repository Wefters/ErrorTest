package dev.wefter.bridge

import org.json.JSONObject

/**
 * Fails on purpose in different ways, to check that native errors and stack traces reach JavaScript and
 * DevTools.
 */
class ErrorTestPlugin(context: android.content.Context, dispatcher: BridgeDispatcher) :
        WefterPlugin(context, dispatcher) {

    /** Succeeds with `{ ok: true }`. */
    @WefterMethod
    fun ping(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        resolve(callback, JSONObject().put("ok", true))
    }

    /** Throws a runtime exception. */
    @WefterMethod
    fun throwRuntime(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        throw RuntimeException(
                "Intentional RuntimeException thrown by ErrorTest.throwRuntime() for devtools native-stack-trace testing"
        )
    }

    /** Throws a null pointer exception. */
    @WefterMethod
    fun throwNullPointer(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        val forcedNull: String? = null
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION") val length = forcedNull!!.length
        resolve(callback, JSONObject().put("length", length))
    }

    /** Throws an exception that wraps another one. */
    @WefterMethod
    fun throwNested(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        throw RuntimeException(
                "Outer failure in ErrorTest.throwNested()",
                IllegalStateException("Inner cause: simulated nested failure")
        )
    }

    /** Rejects through the callback with a custom code instead of throwing. */
    @WefterMethod
    fun rejectWithCode(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        reject(
                callback,
                "CUSTOM_TEST_CODE",
                "Deliberate reject() call from ErrorTest.rejectWithCode() — not a thrown exception, " +
                        "but still carries a native stack pointing at this call site"
        )
    }
}
