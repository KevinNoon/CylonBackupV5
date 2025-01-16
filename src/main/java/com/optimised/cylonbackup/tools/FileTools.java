package com.optimised.cylonbackup.tools;

import com.optimised.cylonbackup.data.entity.Site;
import com.optimised.cylonbackup.data.service.SettingService;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.optimised.cylonbackup.tools.Conversions.tryParseDateTime;
import static com.optimised.cylonbackup.tools.Conversions.tryParseInt;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Log4j2
public class FileTools {

    final static Marker DB = MarkerManager.getMarker("DB");


    public static String zipDirectory(Path source, String siteName, Path backupDir, Path backupOldDir) {
        // get folder name as zip file name
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HHmm");
        String zipFilePath = backupDir + "\\" + siteName + "_" + LocalDateTime.now().format(dtf) + ".zip";

        // Check if there is a site backup existing. If so move it to the old backup folder
        //moveFileToOld(siteName, backupDir, backupOldDir);

        File sourceDir = new File(source.getParent().toString());
        try (
            FileOutputStream fos = new FileOutputStream(zipFilePath);
            ZipOutputStream zipOut = new ZipOutputStream(fos);
        ) {
            zipFileOrDirectory(sourceDir, sourceDir.getName(), zipOut);

            // Close the ZipOutputStream
            zipOut.close();
            fos.close();
        } catch (IOException e) {
            log.error(DB, "Unable to zip : %s%n", e);
        }
        //Clean up the working directory
        try {
            if (Files.exists(source)) PathUtils.deleteDirectory(source);
        } catch (IOException e) {
            log.error(DB, "Unable to delete source : %s%n", e);
        }
        //       FileTools.deleteDirectory(source.toAbsolutePath().toString());
        // Check if there is a site backup existing. If so move it to the old backup folder
        moveFileToOld(siteName, backupDir, backupOldDir);
        return zipFilePath;
    }

    private static void zipFileOrDirectory(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return; // Skip hidden files
        }
        if (fileToZip.isDirectory()) {
            // If the directory is empty, create an entry for it
            if (Objects.requireNonNull(fileToZip.listFiles()).length == 0) {
                zipOut.putNextEntry(new ZipEntry(fileName + "/"));
                zipOut.closeEntry();
            } else {
                // Otherwise, recursively process all files/subdirectories
                File[] children = fileToZip.listFiles();
                assert children != null;
                for (File childFile : children) {
                    zipFileOrDirectory(childFile, fileName + "/" + childFile.getName(), zipOut);
                }
            }
        } else {
            // For files, add them to the zip
            FileInputStream fis = new FileInputStream(fileToZip);
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];

            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }

            // Close the file entry
            fis.close();
        }
    }

    public static void unZipBackup(String backup, String destDir) {

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(backup))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, zipEntry.getName());

                // Create directories for sub-directories in zip
                if (zipEntry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    // Create parent directories if they don't exist
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            log.error(DB, "Unable to unzip : %s%n".formatted(e));
        }
    }

    public static void moveFileToOld(String fileName, Path backupDir, Path backupOldDir) {
        try {
            List<Path> result = findByFileName(backupDir, fileName, false);
            result.stream().sorted(Comparator.reverseOrder()).skip(1).forEach(x -> {
                try {
                    Files.move(x, Paths.get(backupOldDir + "\\" + x.getFileName()), REPLACE_EXISTING);
                } catch (IOException e) {
                    log.error("Unable to delete source : %s%n", e);
                }
            });
        } catch (IOException e) {
            log.error("Unable to delete source : %s%n", e);
        }
    }

    public static Site getSiteFromBackup(Path path) {
        Site site = new Site();
        try (ZipFile zipFile = new ZipFile(path.toFile())) {
            Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
            int foundCount = 0;
            while (zipEntries.hasMoreElements()) {
                ZipEntry entry = zipEntries.nextElement();
                if (entry.getName().toLowerCase().endsWith("siteid.txt")) {
                    Scanner sc = new Scanner(zipFile.getInputStream(entry));
                    if (sc.hasNextLine()) {
                        site.setSiteNumber(tryParseInt(sc.nextLine()));
                    }
                    foundCount++;
                }
                if (entry.getName().toLowerCase().endsWith("sitename.txt")) {
                    Scanner sc = new Scanner(zipFile.getInputStream(entry));
                    if (sc.hasNextLine()) {
                        site.setName(sc.nextLine());
                    }
                    foundCount++;
                }
                if (entry.getName().toLowerCase().endsWith("ccbackup.txt")) {
                    Scanner sc = new Scanner(zipFile.getInputStream(entry));
                    if (sc.hasNextLine()) {
                        if (sc.hasNextLine()) site.setName(sc.nextLine());
                        if (sc.hasNextLine()) site.setDirectory(sc.nextLine());
                        if (sc.hasNextLine()) site.setIDCode(sc.nextLine());
                        if (sc.hasNextLine()) site.setTelephone(sc.nextLine());
                        if (sc.hasNextLine()) site.setRemote(tryParseInt(sc.nextLine()));
                        if (sc.hasNextLine()) site.setNetwork(tryParseInt(sc.nextLine()));
                        if (sc.hasNextLine()) site.setBackupTime(tryParseDateTime(sc.nextLine()));
                        if (sc.hasNextLine()) site.setInternet(tryParseInt(sc.nextLine()));
                        if (sc.hasNextLine()) site.setIpAddr(sc.nextLine());
                        if (sc.hasNextLine()) site.setPort(tryParseInt(sc.nextLine()));
                        if (sc.hasNextLine()) site.setBacNet(tryParseInt(sc.nextLine()));
                        if (sc.hasNextLine()) site.setDefaultType(tryParseInt(sc.nextLine()));

                        site.setAlarmScan(1);
                        site.setExisting(true);
                    }
                    foundCount++;
                }
                if (foundCount >= 3) break;
            }
        } catch (IOException e) {
            log.error("Failed to read backup file reason: {}", e.getMessage());
        }

        return site;
    }

    public static List<Path> findByFileName(Path path, String fileName, boolean includeSubDir) throws IOException {

        List<Path> result;
        int directoryLevel = includeSubDir ? 2 : 1;
        try (Stream<Path> pathStream = Files.find(path,
                directoryLevel,
                (p, basicFileAttributes) -> p.getFileName().toString().contains(fileName))
            .filter(f -> f.toString()
                .substring(f.toString().lastIndexOf('\\') + 1, f.toString().lastIndexOf('_'))
                .equalsIgnoreCase(fileName));
        ) {
            result = pathStream.collect(Collectors.toList());
        }
        return result;
    }

    public static List<String> getSiteBackupNames(Path backupDir, Site site, boolean includeOld) {

        List<String> backups = new ArrayList<>();

        String siteName = site.getName();
        try {
            List<Path> paths = findByFileName(backupDir, siteName, includeOld);
            backups.addAll(paths.stream().map(p -> p.toAbsolutePath()
                .toString()).sorted(Comparator.reverseOrder()).toList());
        } catch (IOException e) {
            log.error("Failed to get list of sites {}", e.getMessage());
        }
        return backups;
    }

    public static void setFilesReadOnly(String dirPath, boolean writable) {
        String[] extensions = {"etg", "ftd", "s32", "set", "stg"};
        List<String> paths = findFiles(Path.of(dirPath), extensions);
        paths.forEach(path -> {
            File file = new File(path);
            boolean set = file.setWritable(writable);
        });
    }

    private static List<String> findFiles(Path path, String[] fileExtensions) {
        if (!Files.isDirectory(path)) {

            log.error(DB, "Path must be a directory!");
        }
        List<String> result = new ArrayList<>();
        {
            try (Stream<Path> walk = Files.walk(path, 5)) {
                if (fileExtensions.length == 0) {
                    result = walk
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.toString().toLowerCase())
                        .collect(Collectors.toList());
                } else {
                    result = walk
                        .filter(p -> !Files.isDirectory(p))
                        .map(p -> p.toString().toLowerCase())
                       // .filter(f -> Arrays.stream(fileExtensions).anyMatch(f::endsWith))
                        .collect(Collectors.toList());
                }
            } catch (IOException e) {
                log.error("Unable to read files {} ", e.getMessage());
            }
        }
        return result;
    }

    public static List<String> listFilesInDirectory(String dir){
        try (Stream<Path> stream = Files.list(Paths.get(dir))) {
            return stream
                .filter(file -> !Files.isDirectory(file))
                .map(Path::toAbsolutePath)
                .map(Path::toString)
                .collect(Collectors.toList());
        } catch (IOException e) {
            log.error("Unable to read directory {} ", e.getMessage());
        }
        return new ArrayList<>();
    }

    public static void deleteAllFilesInDirectory(String dir){
        try {
            FileUtils.cleanDirectory(new File(dir));
        } catch (IOException e) {
            log.error("Unable to delete directory {} ", e.getMessage());
        }
    }

    public static void copyDirectories(File src, File dst,List<String> extensions, Boolean exclude) {
        FileFilter filter;
        if (exclude) {
             filter = createExcludeExtensionFilter(extensions);
        } else {
            filter = createIncludeExtensionFilter(extensions);
        }

        try {
            FileUtils.copyDirectory(src, dst, filter);
        } catch (IOException e) {
            log.error("Failed to copy directory {}",e.getMessage());
        }
    }

    private static FileFilter createExcludeExtensionFilter(List<String> extensions) {
        return file -> {
            if (file.isDirectory()) {
                return true; // Always include directories
            }
            String name = file.getName().toLowerCase();
            return extensions.stream().map(String::toLowerCase).noneMatch(name::endsWith); // Ignore case
            //return extensions.stream().noneMatch(name::endsWith); // Exclude files with these extensions
        };
    }

    private static FileFilter createIncludeExtensionFilter(List<String> extensions) {
        return file -> {
            if (file.isDirectory()) {
                return true; // Always include directories
            }
            String name = file.getName().toLowerCase();
            return extensions.stream().map(String::toLowerCase).anyMatch(name::endsWith); // Ignore case
            //return extensions.stream().noneMatch(name::endsWith); // Exclude files with these extensions
        };
    }
}
