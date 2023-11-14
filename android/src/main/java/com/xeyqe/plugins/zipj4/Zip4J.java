package com.xeyqe.plugins.zipj4;

import android.util.Log;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.progress.ProgressMonitor;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Zip4J {

    private String getEditedPath(String path) {
        if (path.contains("file://")) {
            return path.replace("file://", "");
        }
        return path;
    }
    public void extractAll(String zipFilePath, String destDirectory, final String password, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        destDirectory = getEditedPath(destDirectory);

        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile is not valid!");
            return;
        }

        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            Log.i("myzip4j", "creating folder: " + destDirectory);
            if (!destDir.mkdirs()) {
                callback.onError("Unable to create directory " + destDir + "!");
                return;
            }
        }

        ZipFile zipFile;
        zipFile = new ZipFile(zipFilePath);
        zipFile.setCharset(StandardCharsets.UTF_8);

        try {
            if (password.length() > 0 && zipFile.isEncrypted()) {
                Log.i("myzip4j", "setting password " + password);
                zipFile.setPassword(password.toCharArray());
            }
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
            return;
        }

        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        zipFile.setRunInThread(true);
        try {
            zipFile.extractAll(destDirectory);
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
        }
        takeCareOfProgressListener(progressMonitor, zipFile, zipFilePath + " extracted to " + destDirectory, callback);
    }

    private void takeCareOfProgressListener(ProgressMonitor progressMonitor, ZipFile zipFile, String doneMessage, Zip4JCallback callback) {
        int percentsDone = 0;
        while (!progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
            int percents = progressMonitor.getPercentDone();
            if (percentsDone != percents) {
                percentsDone = percents;
                callback.onProgress(percents);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                callback.onError(e.getLocalizedMessage());
                throw new RuntimeException(e);
            }
        }

        if (progressMonitor.getResult().equals(ProgressMonitor.Result.SUCCESS)) {
            try {
                zipFile.close();
            } catch (IOException e) {
                callback.onError(e.getLocalizedMessage());
                return;
            }
            callback.onProgress(100);
            callback.onDone(doneMessage);
        } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.ERROR)) {
            callback.onError(progressMonitor.getException().getMessage());
        } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.CANCELLED)) {
            callback.onError("Task cancelled");
        }
    }

    public void extractFile(String zipFilePath, String destDirectory, final String password, String fileName, String newFileName, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        destDirectory = getEditedPath(destDirectory);
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile is not valid!");
            return;
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        zipFile.setRunInThread(true);
        try {
            if (newFileName != null && !newFileName.equals("")) {
                zipFile.extractFile(fileName, destDirectory, newFileName);
            } else {
                zipFile.extractFile(fileName, destDirectory);
            }
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
            return;
        }
        takeCareOfProgressListener(progressMonitor, zipFile, "File " + fileName + " extracted to " + destDirectory + ".", callback);
    }

    public void removeFiles(String zipFilePath, final JSArray fileNames, final String password, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile " + zipFilePath + " is not valid!");
            return;
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        List<String> filesToRemove = List.of();
        if (fileNames != null && fileNames.length() > 0) {
            for (int i = 0; i < fileNames.length(); i++) {
                try {
                    String file = fileNames.getString(i);
                    if (!containsFile(zipFilePath, password, file)) {
                        callback.onError("ZipFile doesn't include " + file + "!");
                        return;
                    }
                    filesToRemove.add(file);
                } catch (JSONException e) {
                    callback.onError(e.getLocalizedMessage());
                    return;
                }
            }
        } else {
            callback.onError("Nothing to add.");
            return;
        }
        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        try {
            zipFile.removeFiles(filesToRemove);
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
        }
        takeCareOfProgressListener(progressMonitor, zipFile, "Files removed.", callback);
    }

    public void renameFile(String zipFilePath, final String oldFile, final String newFile, final String password, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile " + zipFilePath + " is not valid!");
            return;
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

        FileHeader fileHeader;
        try {
            fileHeader= zipFile.getFileHeader(oldFile);
        } catch (ZipException e) {
            throw new RuntimeException(e);
        }
        if (fileHeader == null) {
            callback.onError("File " + oldFile + " is not in ZipFile.");
            return;
        }
        try {
            zipFile.renameFile(fileHeader, newFile);
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
            return;
        }
        this.takeCareOfProgressListener(progressMonitor, zipFile, "File " + oldFile + " renamed to " + newFile + ".", callback);
    }

    public void mergeSplitFiles(String zipFilePath, String newZipFilePath, final String password, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        newZipFilePath = getEditedPath(newZipFilePath);
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile is not valid.");
            return;
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        File newFile = new File(newZipFilePath);
        if (newFile.exists()) {
            callback.onError("File " + newZipFilePath + " already exits!");
            return;
        }
        try {
            zipFile.mergeSplitFiles(new File(newZipFilePath));
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
            return;
        }
        takeCareOfProgressListener(progressMonitor, zipFile, "Split files merged.", callback);
    }

    public void getAllFileNames(String zipFilePath, String password, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile is not valid!");
            return;
        }
        List<FileHeader> fileHeaders;
        ZipFile zipFile = new ZipFile(zipFilePath);
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        try {
            fileHeaders = zipFile.getFileHeaders();
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
            return;
        }
        ArrayList<String> files = new ArrayList<>();
        for (FileHeader fileHeader : fileHeaders) {
            files.add(fileHeader.getFileName());
        }
        JSArray fileNames = JSArray.from(files.toArray());
        callback.onDone(new JSObject().put("names", fileNames));
    }

    public void isEncrypted(String zipFilePath, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile is not valid!");
            return;
        }
        try {
            boolean isEncrypted = new ZipFile(zipFilePath).isEncrypted();
            callback.onDone(new JSObject().put("isEncrypted", isEncrypted));
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public void isSplitArchive(String zipFilePath, final String password, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile is not valid!");
            return;
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        try {
            boolean isSplitArchive = zipFile.isSplitArchive();
            callback.onDone(new JSObject().put("isSplitArchive", isSplitArchive));
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public void setComment(String zipFilePath, String comment, final String password, Zip4JCallback callback) {
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile is not valid.");
            return;
        }
        if (comment == null) comment = "";
        zipFilePath = getEditedPath(zipFilePath);
        ZipFile zipFile = new ZipFile(zipFilePath);
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        try {
            zipFile.setComment(comment);
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
        }
        takeCareOfProgressListener(progressMonitor, zipFile, "Comment " + comment + " set.", callback);
    }

    public void getComment(String zipFilePath, final String password, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        ZipFile zipFile = new ZipFile(zipFilePath);
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        try {
            String comment = zipFile.getComment();
            callback.onDone(new JSObject().put("comment", comment));
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
        }
    }

    public boolean isValid(String zipFilePath) {
        zipFilePath = getEditedPath(zipFilePath);
        try {
            new File(zipFilePath);
        } catch (NullPointerException e) {
            return false;
        }
        return new ZipFile(zipFilePath).isValidZipFile();
    }

    public void containsFile(String zipFilePath, String fileName, final String password, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile is not valid!");
            return;
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        FileHeader fileHeader;
        try {
            fileHeader = zipFile.getFileHeader(fileName);
            callback.onDone(new JSObject().put("contains", fileHeader != null));
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
        }

    }

    public boolean containsFile(String zipFilePath, final String password, String fileName) {
        zipFilePath = getEditedPath(zipFilePath);
        if (!isValid(zipFilePath)) {
            return false;
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        if (password != null && !password.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        FileHeader fileHeader;
        try {
            fileHeader = zipFile.getFileHeader(fileName);
        } catch (ZipException e) {
            return false;
        }

        return fileHeader != null;
    }

    public void addFiles(final String zipFilePath, final JSArray files, final String password, Zip4JCallback callback) {
        List<File> filesToAdd = List.of();
        if (files != null && files.length() > 0) {
            for (int i = 0; i < files.length(); i++) {
                try {
                    String path = files.getString(i);
                    File file = new File(path);
                    if (!file.exists()) {
                        callback.onError("File " + path + " doesn't exist.");
                        return;
                    } else if (file.isDirectory()) {
                        callback.onError("File " + path + " is not a file, but a directory.");
                        return;
                    }
                    filesToAdd.add(file);
                } catch (JSONException e) {
                    callback.onError(e.getLocalizedMessage());
                    return;
                }
            }
        } else {
            callback.onError("Nothing to add.");
            return;
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        if (password != null && password.length() > 0) {
            zipFile.setPassword(password.toCharArray());
        }
        zipFile.setCharset(StandardCharsets.UTF_8);
        zipFile.setRunInThread(true);
        try {
            zipFile.addFiles(filesToAdd);
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
            return;
        }
        this.takeCareOfProgressListener(progressMonitor, zipFile, "Files added to " + zipFilePath, callback);
    }

    public void addFolder(final String zipFilePath, final String folder, final String password, Zip4JCallback callback) {
        if (folder == null || folder.equals("")) {
            callback.onError("Nothing to add.");
            return;
        }
        File file = new File(folder);
        if (!file.exists()) {
            callback.onError("Folder " + folder + " doesn't exist.");
            return;
        } else if (file.isFile()) {
            callback.onError("Folder " + folder + " is not a directory, but a file.");
            return;
        }
        ZipFile zipFile = new ZipFile(zipFilePath);
        ProgressMonitor progressMonitor;
        if (password.length() > 0) {
            zipFile.setPassword(password.toCharArray());
        }
        zipFile.setCharset(StandardCharsets.UTF_8);

        progressMonitor = zipFile.getProgressMonitor();
        zipFile.setRunInThread(true);
        try {
            zipFile.addFolder(file);
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
            return;
        }
        this.takeCareOfProgressListener(progressMonitor, zipFile, "Files added to " + zipFilePath + ".", callback);
    }

    public void createSplitZipFile(String zipFilePath, final JSArray files, final long splitLength, final String password, Zip4JCallback callback) {
        zipFilePath = getEditedPath(zipFilePath);
        if (!isValid(zipFilePath)) {
            callback.onError("ZipFile is not valid.");
            return;
        }
        List<File> filesToAdd = List.of();
        if (files != null && files.length() > 0) {
            for (int i = 0; i < files.length(); i++) {
                try {
                    String path = files.getString(i);
                    File file = new File(path);
                    if (!file.exists()) {
                        callback.onError("File " + path + " doesn't exist.");
                        return;
                    } else if (file.isDirectory()) {
                        callback.onError("File " + path + " is not a file, but a directory.");
                        return;
                    }
                    filesToAdd.add(file);
                } catch (JSONException e) {
                    callback.onError(e.getLocalizedMessage());
                    return;
                }
            }
        } else {
            callback.onError("Nothing to add.");
            return;
        }

        ZipFile zipFile = new ZipFile(zipFilePath);
        if (password != null && !zipFilePath.equals("")) {
            zipFile.setPassword(password.toCharArray());
        }
        ProgressMonitor progressMonitor = zipFile.getProgressMonitor();
        try {
            zipFile.createSplitZipFile(filesToAdd, new ZipParameters(), true, splitLength);
        } catch (ZipException e) {
            callback.onError(e.getLocalizedMessage());
            return;
        }
        takeCareOfProgressListener(progressMonitor, zipFile, "SplitZipFile created.", callback);
    }

}

