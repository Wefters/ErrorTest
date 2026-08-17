// @vitest-environment jsdom
import { afterEach, describe, expect, it } from "vitest";
import { installMockBridge, uninstallMockBridge } from "@wefterjs/core/testing";
import { WefterBridgeError } from "@wefterjs/core";
import { ErrorTest } from "../src/index.js";

afterEach(() => {
  uninstallMockBridge();
});

describe("ErrorTest.ping", () => {
  it("resolves with ok: true", async () => {
    installMockBridge({
      "error-test": (method) => {
        if (method === "ping") return { ok: true };
        throw new Error(`unexpected method ${method}`);
      },
    });

    expect(await ErrorTest.ping()).toEqual({ ok: true });
  });
});g

describe("ErrorTest throw methods", () => {
  it("surfaces a native-thrown error as a WefterBridgeError", async () => {
    installMockBridge({
      "error-test": (method) => {
        if (method === "throwRuntime") throw new Error("java.lang.RuntimeException: boom");
        throw new Error(`unexpected method ${method}`);
      },
    });

    const call = ErrorTest.throwRuntime();
    await expect(call).rejects.toBeInstanceOf(WefterBridgeError);
    await expect(call).rejects.toMatchObject({ code: "MOCK_ERROR", message: "java.lang.RuntimeException: boom" });
  });

  it("surfaces a deliberate reject() call the same way", async () => {
    installMockBridge({
      "error-test": (method) => {
        if (method === "rejectWithCode") throw new Error("Deliberate reject() call");
        throw new Error(`unexpected method ${method}`);
      },
    });

    await expect(ErrorTest.rejectWithCode()).rejects.toMatchObject({ code: "MOCK_ERROR" });
  });
});
