package com.santosschein.app;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArchitectureBoundaryTest {

    @Test
    public void domainDoesNotDependOnAndroidRetrofitDtoOrPresentation() throws IOException {
        Path domainPath = sourcePath("com/santosschein/app/domain");
        assertTrue(Files.exists(domainPath));

        try (Stream<Path> files = Files.walk(domainPath)) {
            for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".java"))::iterator) {
                String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
                assertFalse(file + " must not import Android APIs", source.contains("import android."));
                assertFalse(file + " must not import AndroidX APIs", source.contains("import androidx."));
                assertFalse(file + " must not import Retrofit", source.contains("retrofit2"));
                assertFalse(file + " must not import DTOs", source.contains(".data.remote.dto"));
                assertFalse(file + " must not import presentation", source.contains(".presentation."));
            }
        }
    }

    @Test
    public void projectDoesNotUseLocalDatabaseLibraries() throws IOException {
        Path mainPath = sourcePath("com/santosschein/app");
        assertTrue(Files.exists(mainPath));

        try (Stream<Path> files = Files.walk(mainPath)) {
            for (Path file : (Iterable<Path>) files.filter(path -> path.toString().endsWith(".java"))::iterator) {
                String source = new String(Files.readAllBytes(file), StandardCharsets.UTF_8).toLowerCase();
                assertFalse(file + " must not use Room", source.contains("androidx.room"));
                assertFalse(file + " must not use SQLite", source.contains("sqlite"));
                assertFalse(file + " must not use Realm", source.contains("realm"));
            }
        }
    }

    private static Path sourcePath(String packagePath) {
        Path fromRoot = Paths.get("app", "src", "main", "java").resolve(packagePath);
        if (Files.exists(fromRoot)) {
            return fromRoot;
        }
        return Paths.get("src", "main", "java").resolve(packagePath);
    }
}
