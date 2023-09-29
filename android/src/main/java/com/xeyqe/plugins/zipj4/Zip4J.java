package com.xeyqe.plugins.zipj4;

import static java.security.AccessController.getContext;

import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;

import com.getcapacitor.PluginCall;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Zip4J {
    public void unzip(String zipFilePath, String destDirectory, final String password, Zip4JCallback callback) {
        if (zipFilePath.contains("file://")) {
            zipFilePath = zipFilePath.replace("file://", "");
            Log.i("myzip4j", "file://" + zipFilePath + " changed to " + zipFilePath);
        }
        if (destDirectory.contains("file://")) {
            destDirectory = destDirectory.replace("file://", "");
            Log.i("myzip4j", "file://" + destDirectory + " changed to " + destDirectory);
        }

        try {
            new File(zipFilePath);
        } catch (NullPointerException e) {
            callback.onError("Couldn't open file " + zipFilePath + ".");
            return;
        }

        File destDir = new File(destDirectory);
        if (!destDir.exists()) {
            Log.i("myzip4j", "creating folder: " + destDirectory);
            destDir.mkdirs();
        }

        net.lingala.zip4j.ZipFile zipFile = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            zipFile = new net.lingala.zip4j.ZipFile(zipFilePath);
            zipFile.setCharset(StandardCharsets.UTF_8);
        } else {
            zipFile = new net.lingala.zip4j.ZipFile(zipFilePath);
        }

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

        int percentsDone = 0;
        while (!progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
            int percents = progressMonitor.getPercentDone();
            Log.i("myzip4j", "percents: " + percents);
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
            callback.onDone(zipFilePath + " extracted " + destDirectory);
        } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.ERROR)) {
            callback.onError(progressMonitor.getException().getMessage());
        } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.CANCELLED)) {
            callback.onError("Task cancelled");
        }
    }
}

