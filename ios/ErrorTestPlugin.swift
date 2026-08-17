import Foundation

final class ErrorTestPlugin: WefterPlugin {

    // @WefterMethod
    func ping(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        resolve(callback, data: ["ok": true])
    }

    // @WefterMethod
    func throwRuntime(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        throw WefterError(
            code: "RUNTIME_ERROR",
            message: "Intentional error thrown by ErrorTest.throwRuntime() for devtools native-stack-trace testing"
        )
    }

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

    // @WefterMethod
    func rejectWithCode(payload: [String: Any], callback: @escaping (Result<Any, Error>) -> Void) throws {
        reject(
            callback,
            code: "CUSTOM_TEST_CODE",
            message: "Deliberate reject() call from ErrorTest.rejectWithCode() — not a thrown exception"
        )
    }
}
