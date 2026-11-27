package dev.wefter.bridge

import org.json.JSONObject

/**
 * Fails on purpose in different ways, to check that native errors and stack traces reach JavaScript and
 * DevTools.
 */
class ErrorTestPlugin(context: android.content.Context, dispatcher: BridgeDispatcher) :
        WefterPlugin(context, dispatcher) {

    /** Lifecycle callbacks this plugin received, oldest first, for checking plugin lifecycle forwarding. */
    private val lifecycle = mutableListOf<String>()

    override fun onResume() {
        lifecycle.add("resume")
    }

    override fun onPause() {
        lifecycle.add("pause")
    }

    /**
     * After `delayMs`, emits `errorTest:held` with `data`, held until the page listens, to check that held
     * events wait for a listener.
     */
    @WefterMethod
    fun holdEvent(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        val delay = payload.optLong("delayMs", 0)
        val data = payload.optJSONObject("data") ?: JSONObject()
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({ emit("errorTest:held", data, hold = true) }, delay)
        resolve(callback, JSONObject().put("scheduled", true))
    }

    /**
     * Opens the system settings with `launchForResult` and resolves with the result code once the user
     * comes back, to check that plugins can start other screens at any time.
     */
    @WefterMethod
    fun launchForResultTest(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        launchForResult(
                androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(),
                android.content.Intent(android.provider.Settings.ACTION_SETTINGS)
        ) { result -> resolve(callback, JSONObject().put("resultCode", result.resultCode)) }
    }

    /** The lifecycle callbacks received so far, and how many plugin instances exist for this screen. */
    @WefterMethod
    fun lifecycleLog(payload: JSONObject, callback: (Result<Any>) -> Unit) {
        resolve(callback, JSONObject().put("events", org.json.JSONArray(lifecycle)).put("instance", System.identityHashCode(this)))
    }

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
