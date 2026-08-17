import { definePlugin } from "@wefterjs/core";

export interface PingResult {
  ok: true;
}

export const ErrorTest = definePlugin<{
  ping: () => Promise<PingResult>;
  throwRuntime: () => Promise<never>;
  throwNullPointer: () => Promise<never>;
  throwNested: () => Promise<never>;
  rejectWithCode: () => Promise<never>;
}>("error-test", {
  ping: true,
  throwRuntime: true,
  throwNullPointer: true,
  throwNested: true,
  rejectWithCode: true,
});
