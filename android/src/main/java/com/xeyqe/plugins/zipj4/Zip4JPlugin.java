package com.xeyqe.plugins.zipj4;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;


@CapacitorPlugin(name = "Zip4J")
public class Zip4JPlugin extends Plugin {
    private final Zip4J implementation = new Zip4J();

    @PluginMethod
    public void extractAll(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String destDirectory = call.getString("destDirectory");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("extractAllProgressEvent", ret);
            }
        };
        implementation.extractAll(zipFilePath, destDirectory, password, resultCallback);
    }

    @PluginMethod
    public void extractFile(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String destDirectory = call.getString("destDirectory");
        String password = call.getString("password", "");
        String fileName = call.getString("fileName", "");
        String newFileName = call.getString("newFileName", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("extractFileProgressEvent", ret);
            }
        };
        implementation.extractFile(zipFilePath, destDirectory, password, fileName, newFileName, resultCallback);
    }

    @PluginMethod
    public void removeFiles(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        JSArray fileNames = call.getArray("fileNames");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("removeFilesProgressEvent", ret);
            }
        };
        implementation.removeFiles(zipFilePath, fileNames, password, resultCallback);
    }

    @PluginMethod
    public void renameFile(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String oldFile = call.getString("oldFile");
        String newFile = call.getString("newFile");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("renameFileProgressEvent", ret);
            }
        };
        implementation.renameFile(zipFilePath, oldFile, newFile, password, resultCallback);
    }

    @PluginMethod
    public void mergeSplitFiles(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String newZipFilePath = call.getString("newZipFilePath");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("mergeSplitFilesProgressEvent", ret);
            }
        };
        implementation.mergeSplitFiles(zipFilePath, newZipFilePath, password, resultCallback);
    }

    @PluginMethod
    public void getAllFileNames(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("getAllFileNamesProgressEvent", ret);
            }
        };
        implementation.getAllFileNames(zipFilePath, password, resultCallback);
    }

    @PluginMethod
    public void isEncrypted(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("isEncryptedProgressEvent", ret);
            }
        };
        implementation.isEncrypted(zipFilePath, resultCallback);
    }

    @PluginMethod
    public void isSplitArchive(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String password = call.getString("password");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("isSplitArchiveProgressEvent", ret);
            }
        };
        implementation.isSplitArchive(zipFilePath, password, resultCallback);
    }

    @PluginMethod
    public void setComment(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String comment = call.getString("comment", "");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("setCommentProgressEvent", ret);
            }
        };
        implementation.setComment(zipFilePath, comment, password, resultCallback);
    }

    @PluginMethod
    public void getComment(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("getCommentProgressEvent", ret);
            }
        };
        implementation.getComment(zipFilePath, password, resultCallback);
    }


    @PluginMethod
    public void containsFile(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String fileName = call.getString("fileName");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("containsFileProgressEvent", ret);
            }
        };
        implementation.containsFile(zipFilePath, fileName, password, resultCallback);
    }

    @PluginMethod
    public void addFiles(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        JSArray files = call.getArray("files");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("addFilesProgressEvent", ret);
            }
        };
        implementation.addFiles(zipFilePath, files, password, resultCallback);
    }

    @PluginMethod
    public void addFolder(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        String folder = call.getString("folder");
        String password = call.getString("password", "");

        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("addFolderProgressEvent", ret);
            }
        };
        implementation.addFolder(zipFilePath, folder, password, resultCallback);
    }

    @PluginMethod
    public void createSplitZipFile(PluginCall call) {
        String zipFilePath = call.getString("zipFilePath");
        JSArray files = call.getArray("files");
        String password = call.getString("password", "");
        long splitLength = call.getLong("splitLength");


        Zip4JCallback resultCallback = new Zip4JCallback() {
            @Override
            public void onDone(String message) {
                call.resolve(new JSObject().put("message", message));
            }
            @Override
            public void onDone(JSObject obj) {
                call.resolve(obj);
            }
            @Override
            public void onError(String message) {
                call.reject(message);
            }
            @Override
            public void onProgress(int progress) {
                JSObject ret = new JSObject();
                ret.put("progress", progress);
                notifyListeners("createSplitZipFileProgressEvent", ret);
            }
        };
        implementation.createSplitZipFile(zipFilePath, files, splitLength, password, resultCallback);
    }

}
