package com.xeyqe.plugins.zipj4;

import static org.junit.Assert.*;

import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import net.lingala.zip4j.ZipFile;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class Zip4JTest {

    @Rule
    public TemporaryFolder temporary = new TemporaryFolder();

    private final Zip4J implementation = new Zip4J();

    private static class Result implements Zip4JCallback {

        JSObject value;
        String error;
        int completions;
        final List<Integer> progress = new ArrayList<>();

        @Override
        public void onDone(JSObject value) {
            this.value = value;
            completions++;
        }

        @Override
        public void onError(String message) {
            error = message;
            completions++;
        }

        @Override
        public void onProgress(int percent) {
            assertEquals("Progress must precede completion", 0, completions);
            assertTrue(percent >= 0 && percent <= 100);
            progress.add(percent);
        }

        void succeeded() {
            assertEquals(1, completions);
            assertNull(error);
            assertNotNull(value);
        }

        void failed() {
            assertEquals(1, completions);
            assertNotNull(error);
            assertFalse(error.isEmpty());
            assertNull(value);
            assertFalse(progress.contains(100));
        }
    }

    private File source(String name) throws Exception {
        File file = temporary.newFile(name);
        Files.write(file.toPath(), "Archive content with ünicode".getBytes(StandardCharsets.UTF_8));
        return file;
    }

    private JSArray array(String... values) {
        return new JSArray(Arrays.asList(values));
    }

    private File archive(File source) throws Exception {
        File output = new File(temporary.getRoot(), "archive.zip");
        Result result = new Result();
        implementation.addFiles(output.getPath(), array(source.getPath()), null, result);
        result.succeeded();
        assertEquals(Integer.valueOf(100), result.progress.get(result.progress.size() - 1));
        return output;
    }

    @Test
    public void addsRenamesAndRemovesFiles() throws Exception {
        File source = source("first.txt");
        File archive = archive(source);
        Result rename = new Result();
        implementation.renameFile(archive.getPath(), source.getName(), "renamed.txt", null, rename);
        rename.succeeded();
        assertFalse(implementation.containsFile(archive.getPath(), null, source.getName()));
        assertTrue(implementation.containsFile(archive.getPath(), null, "renamed.txt"));
        Result remove = new Result();
        implementation.removeFiles(archive.getPath(), array("renamed.txt"), null, remove);
        remove.succeeded();
        assertFalse(implementation.containsFile(archive.getPath(), null, "renamed.txt"));
    }

    @Test
    public void supportsEncodedFileUrisForInputsOutputsAndComments() throws Exception {
        File source = source("sp ace ü.txt");
        File archive = new File(temporary.getRoot(), "my archive.zip");
        Result add = new Result();
        implementation.addFiles(archive.toURI().toString(), array(source.toURI().toString()), "", add);
        add.succeeded();
        Result comment = new Result();
        implementation.setComment(archive.toURI().toString(), "Hello ü", null, comment);
        comment.succeeded();
        Result read = new Result();
        implementation.getComment(archive.toURI().toString(), null, read);
        read.succeeded();
        assertEquals("Hello ü", read.value.getString("comment"));
        File destination = new File(temporary.getRoot(), "output space");
        Result extract = new Result();
        implementation.extractAll(archive.toURI().toString(), destination.toURI().toString(), null, extract);
        extract.succeeded();
        assertArrayEquals(Files.readAllBytes(source.toPath()), Files.readAllBytes(new File(destination, source.getName()).toPath()));
    }

    @Test
    public void passwordEncryptsNewEntriesAndWrongPasswordFailsOnce() throws Exception {
        File source = source("secret.txt");
        File archive = new File(temporary.getRoot(), "encrypted.zip");
        Result add = new Result();
        implementation.addFiles(archive.getPath(), array(source.getPath()), "secret", add);
        add.succeeded();
        try (ZipFile zip = new ZipFile(archive)) {
            assertTrue(zip.isEncrypted());
        }
        Result wrongPassword = new Result();
        implementation.extractAll(archive.getPath(), new File(temporary.getRoot(), "wrong").getPath(), "wrong", wrongPassword);
        wrongPassword.failed();
        File destination = new File(temporary.getRoot(), "correct");
        Result extract = new Result();
        implementation.extractFile(archive.getPath(), destination.getPath(), "secret", source.getName(), "renamed.txt", extract);
        extract.succeeded();
        assertArrayEquals(Files.readAllBytes(source.toPath()), Files.readAllBytes(new File(destination, "renamed.txt").toPath()));
    }

    @Test
    public void createsAndMergesSplitArchiveAtNewPath() throws Exception {
        File source = source("large.bin");
        byte[] content = new byte[200000];
        new Random(42).nextBytes(content);
        Files.write(source.toPath(), content);
        File archive = new File(temporary.getRoot(), "split.zip");
        Result create = new Result();
        implementation.createSplitZipFile(archive.getPath(), array(source.getPath()), 65536, null, create);
        create.succeeded();
        assertTrue(new File(temporary.getRoot(), "split.z01").isFile());
        assertTrue(implementation.isValid(archive.getPath()));
        File merged = new File(temporary.getRoot(), "merged.zip");
        Result merge = new Result();
        implementation.mergeSplitFiles(archive.getPath(), merged.getPath(), null, merge);
        merge.succeeded();
        try (ZipFile zip = new ZipFile(merged)) {
            assertFalse(zip.isSplitArchive());
            File destination = temporary.newFolder("merged-output");
            zip.extractAll(destination.getPath());
            assertArrayEquals(content, Files.readAllBytes(new File(destination, source.getName()).toPath()));
        }
    }

    @Test
    public void rejectsInvalidInputsWithoutThrowingOrCompletingTwice() throws Exception {
        assertFalse(implementation.isValid(null));
        assertFalse(implementation.isValid(""));
        assertFalse(implementation.isValid("content://archive.zip"));
        assertFalse(implementation.containsFile(null, null, null));
        Result missing = new Result();
        implementation.extractAll(null, null, null, missing);
        missing.failed();
        Result invalid = new Result();
        implementation.getComment(source("invalid.zip").getPath(), null, invalid);
        invalid.failed();
        File archive = archive(source("valid.txt"));
        Result rename = new Result();
        implementation.renameFile(archive.getPath(), "missing.txt", "new.txt", null, rename);
        rename.failed();
        Result remove = new Result();
        implementation.removeFiles(archive.getPath(), array("missing.txt"), null, remove);
        remove.failed();
        Result badDestination = new Result();
        implementation.extractAll(archive.getPath(), source("destination.txt").getPath(), null, badDestination);
        badDestination.failed();
        Result invalidArray = new Result();
        implementation.addFiles(new File(temporary.getRoot(), "bad.zip").getPath(), new JSArray(Arrays.asList(123)), null, invalidArray);
        invalidArray.failed();
        Result split = new Result();
        implementation.createSplitZipFile(new File(temporary.getRoot(), "small.zip").getPath(), array(archive.getPath()), 1, null, split);
        split.failed();
    }

    @Test
    public void folderAdditionListingAndContainsReturnExpectedResults() throws Exception {
        File folder = temporary.newFolder("folder");
        File source = new File(folder, "entry.txt");
        Files.write(source.toPath(), new byte[] { 1, 2, 3 });
        File archive = new File(temporary.getRoot(), "folder.zip");
        Result add = new Result();
        implementation.addFolder(archive.toURI().toString(), folder.toURI().toString(), null, add);
        add.succeeded();
        Result list = new Result();
        implementation.getAllFileNames(archive.getPath(), null, list);
        list.succeeded();
        assertTrue(list.value.getJSONArray("names").toString().contains("folder/entry.txt"));
        Result contains = new Result();
        implementation.containsFile(archive.getPath(), "folder/entry.txt", null, contains);
        contains.succeeded();
        assertTrue(contains.value.getBoolean("contains"));
        Result encrypted = new Result();
        implementation.isEncrypted(archive.getPath(), encrypted);
        encrypted.succeeded();
        assertFalse(encrypted.value.getBoolean("isEncrypted"));
        Result split = new Result();
        implementation.isSplitArchive(archive.getPath(), null, split);
        split.succeeded();
        assertFalse(split.value.getBoolean("isSplitArchive"));
    }
}
