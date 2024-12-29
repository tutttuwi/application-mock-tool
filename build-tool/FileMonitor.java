import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.stream.Collectors;

public class FileMonitor {

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length < 3) {
            System.out.println("Usage: java FileMonitor <srcDir> <distDir> <fileExtensionRegex>");
            return;
        }

        Path srcDir = Paths.get(args[0]);
        Path distDir = Paths.get(args[1]);
        String fileExtensionRegex = args[2];

        Map<Path, Long> fileTimestamps = new HashMap<>();
        initializeFileTimestamps(srcDir, fileTimestamps);

        // Run handleFileChange once at startup
        handleFileChange(srcDir, distDir, fileTimestamps);

        WatchService watchService = FileSystems.getDefault().newWatchService();
        srcDir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_MODIFY);

        System.out.println(srcDir);
        System.out.println(distDir);
        System.out.println(fileExtensionRegex);

        while (true) {
            WatchKey key = watchService.poll();
            if (key != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = srcDir.resolve((Path) event.context());
                    if (Files.isRegularFile(changed) && changed.getFileName().toString().matches(fileExtensionRegex)) {
                        System.out.println("Detected change in file: " + changed);
                        handleFileChange(srcDir, distDir, fileTimestamps);
                    }
                }
                key.reset();
            }

            // Periodically check all files for changes
            if (checkForUpdates(srcDir, fileTimestamps, fileExtensionRegex)) {
                handleFileChange(srcDir, distDir, fileTimestamps);
            }

            Thread.sleep(1000); // Check every second
        }
    }

    private static void initializeFileTimestamps(Path srcDir, Map<Path, Long> fileTimestamps) throws IOException {
        Files.walk(srcDir).filter(Files::isRegularFile).forEach(file -> {
            try {
                fileTimestamps.put(file, Files.getLastModifiedTime(file).toMillis());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    private static boolean checkForUpdates(Path srcDir, Map<Path, Long> fileTimestamps, String fileExtensionRegex) throws IOException {
        boolean updated = false;
        for (Path file : Files.walk(srcDir).filter(Files::isRegularFile).collect(Collectors.toList())) {
            if (file.getFileName().toString().matches(fileExtensionRegex)) {
                long lastModifiedTime = Files.getLastModifiedTime(file).toMillis();
                if (!fileTimestamps.containsKey(file) || fileTimestamps.get(file) != lastModifiedTime) {
                    fileTimestamps.put(file, lastModifiedTime);
                    updated = true;
                }
            }
        }
        return updated;
    }

    private static void handleFileChange(Path srcDir, Path distDir, Map<Path, Long> fileTimestamps) throws IOException {
        clearDirectory(distDir);
        copyDirectory(srcDir, distDir);
        processFiles(distDir);
    }

    private static void clearDirectory(Path directory) throws IOException {
        if (Files.exists(directory)) {
            Files.walkFileTree(directory, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        Files.createDirectories(directory);
    }

    private static void copyDirectory(Path source, Path target) throws IOException {
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                Files.createDirectories(targetDir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static void processFiles(Path directory) throws IOException {
        List<Path> allFiles = Files.walk(directory).filter(Files::isRegularFile).collect(Collectors.toList());

        for (Path file : allFiles) {
            StringBuilder content = new StringBuilder();

            Charset charset = detectCharset(file);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile()), charset))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("<!-- include::")) {
                        String includeFileName = line.substring(line.indexOf("<!-- include::") + 14, line.indexOf(" -->"));
                        Optional<Path> includeFile = allFiles.stream().filter(f -> f.getFileName().toString().equals(includeFileName)).findFirst();
                        if (includeFile.isPresent()) {
                            Charset includeCharset = detectCharset(includeFile.get());
                            try (BufferedReader includeReader = new BufferedReader(new InputStreamReader(new FileInputStream(includeFile.get().toFile()), includeCharset))) {
                                includeReader.lines().forEach(content::append);
                            }
                        } else {
                            content.append(line);
                        }
                    } else {
                        content.append(line).append(System.lineSeparator());
                    }
                }
            }

            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.toFile()), charset))) {
                writer.write(content.toString());
            }
        }
    }

    private static Charset detectCharset(Path file) {
        try (InputStream is = new FileInputStream(file.toFile())) {
            byte[] buffer = new byte[3];
            is.read(buffer, 0, 3);
            if (buffer[0] == (byte) 0xEF && buffer[1] == (byte) 0xBB && buffer[2] == (byte) 0xBF) {
                return StandardCharsets.UTF_8; // UTF-8 with BOM
            } else if (buffer[0] == (byte) 0xFE && buffer[1] == (byte) 0xFF) {
                return StandardCharsets.UTF_16BE; // UTF-16BE
            } else if (buffer[0] == (byte) 0xFF && buffer[1] == (byte) 0xFE) {
                return StandardCharsets.UTF_16LE; // UTF-16LE
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Charset.forName("Shift_JIS"); // Default fallback
    }
}
