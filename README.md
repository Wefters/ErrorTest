# @wefterjs/error-test

Diagnostic testing plugin used to verify native bridge error handling, exception capture, and promise rejections.

## Purpose

The plugin exposes methods that deliberately trigger different failure modes in native code (Kotlin on Android, Swift on iOS). It is used primarily by Wefter test suites and `@wefterjs/devtools` to confirm that:

- Unhandled native exceptions do not crash the host application.
- Native stack traces and causal chains are captured and serialized into JSON.
- Custom rejection codes pass through the bridge into JavaScript `WefterBridgeError` instances.
- Successful ping operations return expected objects.

## Installation and setup

Install the plugin package in your Wefter application:

```bash
wefter add @wefterjs/error-test
wefter sync
```

## JavaScript API reference

Import `ErrorTest` from `@wefterjs/error-test`:

```ts
import { ErrorTest } from "@wefterjs/error-test";
import { WefterBridgeError } from "@wefterjs/core";
```

### Ping

Verifies standard successful bridge communication:

```ts
const result = await ErrorTest.ping();
console.log(result.ok); // true
```

### Throw runtime exception

Triggers an intentional unhandled native runtime exception:

```ts
try {
  await ErrorTest.throwRuntime();
} catch (error) {
  if (error instanceof WefterBridgeError) {
    console.log("Error code:", error.code);
    console.log("Error message:", error.message);
  }
}
```

### Throw null pointer exception

Triggers a null pointer dereference on Android and nil force-unwrap on iOS:

```ts
try {
  await ErrorTest.throwNullPointer();
} catch (error) {
  console.log("Null pointer caught:", error.message);
}
```

### Throw nested exceptions

Triggers an exception with an inner cause to test nested exception serialization:

```ts
try {
  await ErrorTest.throwNested();
} catch (error) {
  console.log("Nested error caught:", error.message);
}
```

### Explicit bridge rejection

Invokes the bridge `reject` helper with a custom error code:

```ts
try {
  await ErrorTest.rejectWithCode();
} catch (error) {
  if (error instanceof WefterBridgeError) {
    console.log("Custom code received:", error.code); // "CUSTOM_TEST_CODE"
  }
}
```

## Native exception behavior

When a native plugin method throws:

1. The method runner catches the throwable inside a try-catch boundary.
2. The exception message and stack trace are extracted.
3. The response is formatted into a bridge error payload with code `PLUGIN_THREW` (or the explicit code if rejected via `reject()`).
4. JavaScript rejects the promise with a `WefterBridgeError`, preventing host process termination.

## License

[MIT](LICENSE) © 2026 Sandip Ghimire
