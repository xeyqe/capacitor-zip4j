package com.xeyqe.plugins.zipj4;

import com.getcapacitor.JSObject;

/** Receives progress followed by exactly one completion or error on the operation's worker thread. */
public interface Zip4JCallback {
    default void onDone(String message) {
        onDone(new JSObject().put("message", message));
    }

    void onDone(JSObject result);

    void onError(String message);

    default void onProgress(int percent) {}
}
