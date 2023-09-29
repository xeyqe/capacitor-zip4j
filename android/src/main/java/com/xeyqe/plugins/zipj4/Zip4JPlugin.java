package com.xeyqe.plugins.zipj4;

import com.getcapacitor.Bridge;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "Zip4J")
public class Zip4JPlugin extends Plugin {
    private Zip4J implementation = new Zip4J();

    @PluginMethod
    public void unzip(PluginCall call) {
        String source = call.getString("source");
        String destination = call.getString("destination");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }

            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("progressEvent", ret);
            }
        };
        implementation.unzip(source, destination, password, resultCallback);
    }

}
