package com.xeyqe.plugins.zipj4;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

@CapacitorPlugin(name = "Zip4J")
public class Zip4JPlugin extends Plugin {

    private final Zip4J implementation = new Zip4J();
    // Serialize operations to avoid concurrent modifications through this plugin.
    private final ExecutorService worker = Executors.newSingleThreadExecutor();

    private interface Operation {
        void run(Zip4JCallback callback);
    }

    private void submit(PluginCall call, String event, Operation operation) {
        AtomicBoolean completed = new AtomicBoolean();
        Zip4JCallback callback = new Zip4JCallback() {
            @Override
            public void onDone(JSObject result) {
                if (completed.compareAndSet(false, true)) {
                    call.resolve(result);
                }
            }

            @Override
            public void onError(String message) {
                if (completed.compareAndSet(false, true)) {
                    call.reject(message == null || message.isEmpty() ? "Archive operation failed." : message);
                }
            }

            @Override
            public void onProgress(int percent) {
                if (!completed.get()) {
                    notifyListeners(event, new JSObject().put("progress", percent));
                }
            }
        };
        try {
            worker.execute(() -> {
                try {
                    operation.run(callback);
                } catch (RuntimeException e) {
                    callback.onError(e.getMessage());
                }
            });
        } catch (RejectedExecutionException e) {
            callback.onError("Zip4J plugin has been destroyed.");
        }
    }

    @Override
    protected void handleOnDestroy() {
        // Let accepted operations finish and release their resources.
        worker.shutdown();
        super.handleOnDestroy();
    }

    @PluginMethod
    public void extractAll(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String destDirectory = call.getString("destDirectory");
        String password = call.getString("password", "");
        submit(call, "extractAllProgressEvent", callback -> implementation.extractAll(zipFilePath, destDirectory, password, callback));
    }

    @PluginMethod
    public void extractFile(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String destDirectory = call.getString("destDirectory");
        String password = call.getString("password", "");
        String fileName = call.getString("fileName", "");
        String newFileName = call.getString("newFileName", "");
        submit(call, "extractFileProgressEvent", callback ->
            implementation.extractFile(zipFilePath, destDirectory, password, fileName, newFileName, callback)
        );
    }

    @PluginMethod
    public void removeFiles(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        JSArray fileNames = call.getArray("fileNames");
        String password = call.getString("password", "");
        submit(call, "removeFilesProgressEvent", callback -> implementation.removeFiles(zipFilePath, fileNames, password, callback));
    }

    @PluginMethod
    public void renameFile(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String oldFile = call.getString("oldFile");
        String newFile = call.getString("newFile");
        String password = call.getString("password", "");
        submit(call, "renameFileProgressEvent", callback -> implementation.renameFile(zipFilePath, oldFile, newFile, password, callback));
    }

    @PluginMethod
    public void mergeSplitFiles(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String newZipFilePath = call.getString("newZipFilePath");
        String password = call.getString("password", "");
        submit(call, "mergeSplitFilesProgressEvent", callback ->
            implementation.mergeSplitFiles(zipFilePath, newZipFilePath, password, callback)
        );
    }

    @PluginMethod
    public void getAllFileNames(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String password = call.getString("password", "");
        submit(call, "getAllFileNamesProgressEvent", callback -> implementation.getAllFileNames(zipFilePath, password, callback));
    }

    @PluginMethod
    public void isEncrypted(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        submit(call, "isEncryptedProgressEvent", callback -> implementation.isEncrypted(zipFilePath, callback));
    }

    @PluginMethod
    public void isSplitArchive(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String password = call.getString("password");
        submit(call, "isSplitArchiveProgressEvent", callback -> implementation.isSplitArchive(zipFilePath, password, callback));
    }

    @PluginMethod
    public void setComment(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String comment = call.getString("comment", "");
        String password = call.getString("password", "");
        submit(call, "setCommentProgressEvent", callback -> implementation.setComment(zipFilePath, comment, password, callback));
    }

    @PluginMethod
    public void getComment(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String password = call.getString("password", "");
        submit(call, "getCommentProgressEvent", callback -> implementation.getComment(zipFilePath, password, callback));
    }

    @PluginMethod
    public void containsFile(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String fileName = call.getString("fileName");
        String password = call.getString("password", "");
        submit(call, "containsFileProgressEvent", callback -> implementation.containsFile(zipFilePath, fileName, password, callback));
    }

    @PluginMethod
    public void addFiles(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        JSArray files = call.getArray("files");
        String password = call.getString("password", "");
        submit(call, "addFilesProgressEvent", callback -> implementation.addFiles(zipFilePath, files, password, callback));
    }

    @PluginMethod
    public void addFolder(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String folder = call.getString("folder");
        String password = call.getString("password", "");
        submit(call, "addFolderProgressEvent", callback -> implementation.addFolder(zipFilePath, folder, password, callback));
    }

    @PluginMethod
    public void createSplitZipFile(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        JSArray files = call.getArray("files");
        String password = call.getString("password", "");
        Long splitLength = call.getLong("splitLength");
        if (splitLength == null) {
            call.reject("splitLength is required.");
            return;
        }
        submit(call, "createSplitZipFileProgressEvent", callback ->
            implementation.createSplitZipFile(zipFilePath, files, splitLength, password, callback)
        );
    }
}
