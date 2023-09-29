package com.xeyqe.plugins.zipj4;

public interface Zip4JCallback {
    void onDone(String message);
    void onError(String message);
    void onProgress(int percents);
}
