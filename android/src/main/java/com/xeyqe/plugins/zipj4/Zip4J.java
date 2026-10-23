package com.xeyqe.plugins.zipj4;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.model.enums.EncryptionMethod;
import net.lingala.zip4j.progress.ProgressMonitor;
import org.json.JSONException;

/** Archive operations run on the calling thread; the plugin supplies a background worker. */
public class Zip4J {

    private interface ArchiveOperation {
        JSObject run(ZipFile archive) throws IOException, JSONException;
    }

    private interface ArchiveTask {
        void run() throws IOException;
    }

    private static String requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
        return value;
    }

    private static File toFile(String path) {
        requireText(path, "Path");
        if (path.startsWith("file:")) {
            // Capacitor file URIs can contain literal spaces. Preserve existing escapes.
            return new File(URI.create(path.replace(" ", "%20")));
        }
        if (path.contains("://")) {
            throw new IllegalArgumentException("Only local paths and file URIs are supported.");
        }
        return new File(path);
    }

    private static ZipFile openArchive(String path, String password) {
        ZipFile archive = new ZipFile(toFile(path));
        archive.setCharset(StandardCharsets.UTF_8);
        if (password != null && !password.isEmpty()) {
            archive.setPassword(password.toCharArray());
        }
        return archive;
    }

    private void execute(String path, String password, boolean mustExist, Zip4JCallback callback, ArchiveOperation operation) {
        JSObject result;
        try (ZipFile archive = openArchive(path, password)) {
            if (mustExist && !archive.isValidZipFile()) {
                throw new IOException("ZipFile is not valid: " + path);
            }
            result = operation.run(archive);
        } catch (IOException | JSONException | IllegalArgumentException e) {
            String message = e.getMessage();
            callback.onError(message == null || message.isEmpty() ? "Archive operation failed." : message);
            return;
        }
        // Resolve only after resources have been closed successfully.
        callback.onDone(result);
    }

    private JSObject runTask(ZipFile archive, Zip4JCallback callback, String message, ArchiveTask task) throws IOException {
        archive.setRunInThread(true);
        task.run();
        ProgressMonitor monitor = archive.getProgressMonitor();
        int previous = -1;
        boolean interrupted = false;
        while (monitor.getState() == ProgressMonitor.State.BUSY) {
            int progress = Math.min(99, Math.max(0, monitor.getPercentDone()));
            if (!interrupted && progress != previous) {
                previous = progress;
                callback.onProgress(progress);
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                monitor.setCancelAllTasks(true);
                interrupted = true;
                // Wait for the archive task to stop before closing its resources.
            }
        }
        if (interrupted) {
            Thread.currentThread().interrupt();
            throw new IOException("Archive operation interrupted.");
        }
        if (monitor.getResult() == ProgressMonitor.Result.CANCELLED) {
            throw new IOException("Task cancelled.");
        }
        if (monitor.getResult() != ProgressMonitor.Result.SUCCESS) {
            Exception error = monitor.getException();
            throw new IOException(error == null ? "Archive operation failed." : error.getMessage(), error);
        }
        callback.onProgress(100);
        return new JSObject().put("message", message);
    }

    private static File destination(String path) throws IOException {
        File directory = toFile(path);
        if (!directory.isDirectory() && !directory.mkdirs() && !directory.isDirectory()) {
            throw new IOException("Unable to create directory " + directory + ".");
        }
        return directory;
    }

    private static List<String> strings(JSArray values, String name) throws JSONException {
        if (values == null || values.length() == 0) {
            throw new IllegalArgumentException(name + " must not be empty.");
        }
        List<String> result = new ArrayList<>();
        for (int i = 0; i < values.length(); i++) {
            Object value = values.get(i);
            if (!(value instanceof String)) {
                throw new IllegalArgumentException(name + " must contain only strings.");
            }
            result.add(requireText((String) value, name + "[" + i + "]"));
        }
        return result;
    }

    private static List<File> sourceFiles(JSArray values) throws JSONException, IOException {
        List<File> result = new ArrayList<>();
        for (String path : strings(values, "files")) {
            File file = toFile(path);
            if (!file.isFile()) {
                throw new IOException("Source is not a file: " + path);
            }
            result.add(file);
        }
        return result;
    }

    private static ZipParameters parameters(String password) {
        ZipParameters parameters = new ZipParameters();
        if (password != null && !password.isEmpty()) {
            parameters.setEncryptFiles(true);
            parameters.setEncryptionMethod(EncryptionMethod.AES);
        }
        return parameters;
    }

    public void extractAll(String path, String destination, String password, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) -> {
            File directory = destination(destination);
            return runTask(archive, callback, path + " extracted to " + destination, () -> archive.extractAll(directory.getPath()));
        });
    }

    public void extractFile(String path, String destination, String password, String fileName, String newFileName, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) -> {
            requireText(fileName, "fileName");
            File directory = destination(destination);
            return runTask(archive, callback, "File " + fileName + " extracted to " + destination + ".", () -> {
                if (newFileName == null || newFileName.isEmpty()) {
                    archive.extractFile(fileName, directory.getPath());
                } else {
                    archive.extractFile(fileName, directory.getPath(), newFileName);
                }
            });
        });
    }

    public void removeFiles(String path, JSArray fileNames, String password, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) -> {
            List<String> names = strings(fileNames, "fileNames");
            for (String name : names) {
                if (archive.getFileHeader(name) == null) {
                    throw new IOException("ZipFile doesn't include " + name + "!");
                }
            }
            return runTask(archive, callback, "Files removed.", () -> archive.removeFiles(names));
        });
    }

    public void renameFile(String path, String oldFile, String newFile, String password, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) -> {
            requireText(oldFile, "oldFile");
            requireText(newFile, "newFile");
            FileHeader header = archive.getFileHeader(oldFile);
            if (header == null) {
                throw new IOException("File " + oldFile + " is not in ZipFile.");
            }
            return runTask(archive, callback, "File " + oldFile + " renamed to " + newFile + ".", () ->
                archive.renameFile(header, newFile)
            );
        });
    }

    public void mergeSplitFiles(String path, String newPath, String password, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) -> {
            File output = toFile(newPath);
            if (output.exists()) {
                throw new IOException("File " + newPath + " already exists!");
            }
            return runTask(archive, callback, "Split files merged.", () -> archive.mergeSplitFiles(output));
        });
    }

    public void getAllFileNames(String path, String password, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) -> {
            JSArray names = new JSArray();
            for (FileHeader header : archive.getFileHeaders()) {
                names.put(header.getFileName());
            }
            return new JSObject().put("names", names);
        });
    }

    public void isEncrypted(String path, Zip4JCallback callback) {
        execute(path, null, true, callback, (archive) -> new JSObject().put("isEncrypted", archive.isEncrypted()));
    }

    public void isSplitArchive(String path, String password, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) -> new JSObject().put("isSplitArchive", archive.isSplitArchive()));
    }

    public void setComment(String path, String comment, String password, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) ->
            runTask(archive, callback, "Comment " + (comment == null ? "" : comment) + " set.", () ->
                archive.setComment(comment == null ? "" : comment)
            )
        );
    }

    public void getComment(String path, String password, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) -> {
            String comment = archive.getComment();
            return new JSObject().put("comment", comment == null ? "" : comment);
        });
    }

    public boolean isValid(String path) {
        try (ZipFile archive = openArchive(path, null)) {
            return archive.isValidZipFile();
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }

    public void containsFile(String path, String fileName, String password, Zip4JCallback callback) {
        execute(path, password, true, callback, (archive) ->
            new JSObject().put("contains", archive.getFileHeader(requireText(fileName, "fileName")) != null)
        );
    }

    public boolean containsFile(String path, String password, String fileName) {
        try (ZipFile archive = openArchive(path, password)) {
            return archive.isValidZipFile() && archive.getFileHeader(requireText(fileName, "fileName")) != null;
        } catch (IOException | IllegalArgumentException e) {
            return false;
        }
    }

    public void addFiles(String path, JSArray files, String password, Zip4JCallback callback) {
        execute(path, password, false, callback, (archive) -> {
            List<File> sources = sourceFiles(files);
            return runTask(archive, callback, "Files added to " + path, () -> archive.addFiles(sources, parameters(password)));
        });
    }

    public void addFolder(String path, String folder, String password, Zip4JCallback callback) {
        execute(path, password, false, callback, (archive) -> {
            File source = toFile(folder);
            if (!source.isDirectory()) {
                throw new IOException("Source is not a directory: " + folder);
            }
            return runTask(archive, callback, "Files added to " + path + ".", () -> archive.addFolder(source, parameters(password)));
        });
    }

    public void createSplitZipFile(String path, JSArray files, long splitLength, String password, Zip4JCallback callback) {
        execute(path, password, false, callback, (archive) -> {
            if (splitLength < 65536) {
                throw new IllegalArgumentException("splitLength must be at least 65536 bytes.");
            }
            List<File> sources = sourceFiles(files);
            return runTask(archive, callback, "SplitZipFile created.", () ->
                archive.createSplitZipFile(sources, parameters(password), true, splitLength)
            );
        });
    }
}
