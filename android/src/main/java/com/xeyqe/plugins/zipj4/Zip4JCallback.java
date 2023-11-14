package com.xeyqe.plugins.zipj4;

import com.getcapacitor.JSObject;

public interface Zip4JCallback {
    void onDone(String message);
    void onDone(JSObject obj);
    void onError(String message);
    void onProgress(int percents);
}
