import Foundation

/// Fails on purpose in different ways, to check that native errors and stack traces reach JavaScript and
/// DevTools.
final class ErrorTestPlugin: WefterPlugin {

    /// Lifecycle callbacks this plugin received, oldest first, for checking plugin lifecycle forwarding.
    private var lifecycle: [String] = []

    override func appDidBecomeActive() { lifecycle.append("active") }
    override func appWillResignActive() { lifecycle.append("inactive") }
    override func appDidEnterBackground() { lifecycle.append("background") }
    override func appWillEnterForeground() { lifecycle.append("foreground") }

    /// After `delayMs`, emits `errorTest:held` with `data`, held until the page listens, to check that held
    /// events wait for a listener.
    // @WefterMethod
    func holdEvent(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        let delay = (payload["delayMs"] as? NSNumber)?.doubleValue ?? 0
        let data = payload["data"] as? [String: Any] ?? [:]
        DispatchQueue.main.asyncAfter(deadline: .now() + delay / 1000) { [weak self] in
            self?.emit("errorTest:held", data, hold: true)
        }
        resolve(callback, data: ["scheduled": true])
    }

    /// Android only: iOS has no activity results.
    // @WefterMethod
    func launchForResultTest(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        reject(callback, code: "UNAVAILABLE", message: "launchForResultTest is Android only")
    }

    /// The lifecycle callbacks received so far, and this plugin instance's identity.
    // @WefterMethod
    func lifecycleLog(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        resolve(callback, data: ["events": lifecycle, "instance": ObjectIdentifier(self).hashValue])
    }

    /// Succeeds with `{ ok: true }`.
    // @WefterMethod
    func ping(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        resolve(callback, data: ["ok": true])
    }

    /// Throws a `RUNTIME_ERROR`.
    // @WefterMethod
    func throwRuntime(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        throw WefterError(
            code: "RUNTIME_ERROR",
            message: "Intentional error thrown by ErrorTest.throwRuntime() for devtools native-stack-trace testing"
        )
    }

    /// Fails a forced cast of a null value, the Swift counterpart of a null pointer error.
    // @WefterMethod
    func throwNullPointer(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        let payloadValue: Any = NSNull()
        guard let forced = payloadValue as? String else {
            throw WefterError(
                code: "TYPE_CAST_FAILED",
                message: "Forced cast failed in ErrorTest.throwNullPointer() — Swift's analogue of a Java NullPointerException"
            )
        }
        resolve(callback, data: ["value": forced])
    }

    /// Throws an error that wraps another one.
    // @WefterMethod
    func throwNested(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        do {
            throw WefterError(code: "INNER_CAUSE", message: "Inner cause: simulated nested failure")
        } catch {
            throw WefterError(
                code: "OUTER_FAILURE",
                message: "Outer failure in ErrorTest.throwNested() — caused by: \(error.localizedDescription)"
            )
        }
    }

    /// Rejects through the callback with a custom code instead of throwing.
    // @WefterMethod
    func rejectWithCode(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        reject(
            callback,
            code: "CUSTOM_TEST_CODE",
            message: "Deliberate reject() call from ErrorTest.rejectWithCode() — not a thrown exception"
        )
    }
}
