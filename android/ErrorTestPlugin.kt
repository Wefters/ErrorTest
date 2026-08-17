package dev.wefter.bridge

import org.json.JSONObject

class ErrorTestPlugin(context: android.content.Context, dispatcher: BridgeDispatcher) :
        WefterPlugin(context, dispatcher) {

    @WefterMethod
    fun ping(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        resolve(callback, JSONObject().put("ok", true))
    }

    @WefterMethod
    fun throwRuntime(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        throw RuntimeException(
                "Intentional RuntimeException thrown by ErrorTest.throwRuntime() for devtools native-stack-trace testing"
        )
    }

    @WefterMethod
    fun throwNullPointer(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        val forcedNull: String? = null
        @Suppress("UNNECESSARY_NOT_NULL_ASSERTION") val length = forcedNull!!.length
        resolve(callback, JSONObject().put("length", length))
    }

    @WefterMethod
    fun throwNested(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        throw RuntimeException(
                "Outer failure in ErrorTest.throwNested()",
                IllegalStateException("Inner cause: simulated nested failure")
        )
    }

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
