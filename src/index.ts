/** A test plugin that fails on purpose, for checking how native errors reach JavaScript and DevTools. */

import { definePlugin } from "@wefterjs/core";

/** Answer to {@link ErrorTest.ping}. */
export interface PingResult {
  ok: true;
}

/** Methods that each fail in a different native way. */
export const ErrorTest = definePlugin<{
  /** Succeeds; checks that the plugin is reachable. */
  ping: () => Promise<PingResult>;
  /** Throws a runtime exception from native code. */
  throwRuntime: () => Promise<never>;
  /** Fails on a null value in native code. */
  throwNullPointer: () => Promise<never>;
  /** Throws an exception wrapped in another one. */
  throwNested: () => Promise<never>;
  /** Rejects cleanly with a custom error code. */
  rejectWithCode: () => Promise<never>;
  /** After `delayMs`, emits `errorTest:held` with `data`, held until the page listens. */
  holdEvent: (options: { delayMs?: number; data?: Record<string, unknown> }) => Promise<{ scheduled: true }>;
  /** The lifecycle callbacks the plugin received, and its instance id. */
  lifecycleLog: () => Promise<{ events: string[]; instance: number }>;
  /** Android: opens the system settings through `launchForResult` and resolves when the user comes back. */
  launchForResultTest: () => Promise<{ resultCode: number }>;
}>("error-test", {
  ping: true,
  throwRuntime: true,
  throwNullPointer: true,
  throwNested: true,
  rejectWithCode: true,
  holdEvent: true,
  lifecycleLog: true,
  launchForResultTest: true,
});
