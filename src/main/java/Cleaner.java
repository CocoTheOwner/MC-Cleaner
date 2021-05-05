import java.io.*;
import java.nio.file.Files;
import java.util.*;

//TODO: import com.google.gson.*;
//TODO: import org.apache.commons.io.FileUtils;

public class Cleaner {
    //TODO: private static final String path = System.getProperty("user.dir") + "\\";
    //TODO: private static final String conf = "CleanerConfig.json";
    private static final List<File> dirs = new ArrayList<>();
    //TODO: private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static List<File> files;

    public static void main(String[] args) throws IOException {

        // Print starting
        print("Starting cleaner");

        // Load config
        //TODO: JsonObject config = new Config(path + conf, gson).getConfig();

        // Save files
        //TODO: files = getFiles(new File(config.get("serverFolder").getAsString()));
        files = getFiles(new File(System.getProperty("user.dir")));
        // Retrieve file, extension and folder filters
        /*TODO:
        Boolean printDeletedFolders = config.get("printDeletedFolders").getAsBoolean(),
        Boolean printRemainingFolders = config.get("printRemainingFolders").getAsBoolean(),
        Boolean printDeletedFiles = config.get("printDeletedFiles").getAsBoolean(),
        Boolean printRemainingFiles = config.get("printRemainingFiles").getAsBoolean()
        List<String> fileFilters = new ArrayList<>();
        List<String> extensionFilters = new ArrayList<>();
        List<String> folderFilters = new ArrayList<>();
        List<String> blacklist = new ArrayList<>();
        JsonArray fileFJson = config.get("files").getAsJsonArray();
        for (int i = 0; i < fileFJson.size(); i++){
            fileFilters.add(fileFJson.get(i).getAsString());
        }
        JsonArray extensionFJson = config.get("extensions").getAsJsonArray();
        for (int i = 0; i < extensionFJson.size(); i++){
            extensionFilters.add(extensionFJson.get(i).getAsString());
        }
        JsonArray folderFJson = config.get("folders").getAsJsonArray();
        for (int i = 0; i < folderFJson.size(); i++){
            folderFilters.add(folderFJson.get(i).getAsString());
        }
        JsonArray blacklistFJSon = config.get("blacklist").getAsJsonArray();
        for (int i = 0; i < blacklistFJSon.size(); i++){
            blacklist.add(blacklistFJSon.get(i).getAsString());
        }
         */
        List<String> fileFilters = Arrays.asList("eula.txt", "server.properties", "ops.json", "paper.yml", "spigot.yml");
        List<String> extensionFilters = Arrays.asList(".bat", ".jar");
        List<String> folderFilters = Arrays.asList("packs", "logs", "Pl3xMap");

        Boolean printDeletedFolders = false;
        Boolean printRemainingFolders = false;
        Boolean printDeletedFiles = false;
        Boolean printRemainingFiles = false;

        // Clean retrieved files
        cleanFiles(
                //TODO: blacklist,
                fileFilters,
                extensionFilters,
                folderFilters,
                printDeletedFolders,
                printRemainingFolders,
                printDeletedFiles,
                printRemainingFiles
        );

        // Print finish
        print("Finished cleaning");
    }

    public static List<File> getFiles(File directory) {

        // Get all files/dirs in directory
        File[] fList = directory.listFiles();

        // Make a list for the local files (not stored to global "files" var)
        List<File> files = new ArrayList<>();

        // Loop over all files/dirs
        for (File f : fList){

            // Make sure file exists
            assert f != null;

            // Add file to fileList if it's a file
            if (f.isFile()){
                files.add(f);

            // Add all files in dir (and subDirs) to fileList
            } else if (f.isDirectory()){
                dirs.add(f);
                files.addAll(getFiles(f));
            }
        }

        // Return the found files
        return files;
    }

    private static void cleanFiles(
            List<String> filterFiles,
            List<String> filterExtensions,
            List<String> filterFolders,
            Boolean deletedFiles,
            Boolean deletedFolders,
            Boolean remainingFiles,
            Boolean remainingFolders
    ) throws IOException {

        // Store delFileSize
        long delFileSize = 0;

        // Make empty arrays for deleted/saved files/folders
        List<File> delFiles = new ArrayList<>();
        List<File> delFolders = new ArrayList<>();
        List<File> saveFiles = new ArrayList<>();
        List<File> saveFolders = new ArrayList<>();
        List<File> missingFiles = new ArrayList<>();
        List<File> missingFolders = new ArrayList<>();
        List<File> failFiles = new ArrayList<>();
        List<File> failFolders = new ArrayList<>();

        // Delete blacklisted folder directories
        /*TODO:
        for (File d : dirs){
            for (String blacklisted : blacklist) {
                if (d.getAbsolutePath().equalsIgnoreCase(blacklisted)){
                    try {
                        FileUtils.deleteDirectory(d);
                    } catch (IOException e){
                        if (!d.exists()){
                            error("Failed to delete: " + d.getAbsolutePath());
                            failFolders.add(d);
                        }
                    }
                }
            }
        }*/

        // Check all files to see if they're supposed to be filtered, or should be deleted
        for (File f : files){

            // Check if file exists (perhaps it was removed during blacklist)
            if (!f.exists()){
                missingFiles.add(f);
                continue;
            }

            // Set delete var
            boolean delete = true;

            // Check all filter files
            for (String filterFile : filterFiles) {
                if (f.getName().startsWith(filterFile)) {
                    delete = false;
                    break;
                }
            }

            // Check if should stop
            if (!delete) {
                // Add file to saved list
                saveFiles.add(f);
                continue;
            }

            // Check all filter extensions
            for (String filterExt : filterExtensions) {
                if (f.getName().endsWith(filterExt)) {
                    delete = false;
                    break;
                }
            }

            // Check if should stop
            if (!delete) {
                // Add file to saved list
                saveFiles.add(f);
                continue;
            }

            // Check all filter folders
            for (String filterFolder : filterFolders) {
                if (f.getParentFile().getAbsolutePath().contains(filterFolder)) {
                    delete = false;
                    break;
                }
            }

            // If should be deleted
            if (delete) {

                // Delete file
                delFileSize += Files.size(f.toPath());
                if (!f.delete()){

                    // If failed, send warning
                    warn("Tried deleting " + f.getAbsolutePath() + " but failed");

                    // Add to failed list
                    failFiles.add(f);

                } else {

                    // Add file to deleted list
                    delFiles.add(f);
                }

                // If should be saved
            } else {

                // Add file to saved list
                saveFiles.add(f);
            }
        }

        // Check all folders to see if they are empty and not filtered
        for (File d : dirs){

            // Make sure dir exists, may be deleted with blacklist remove
            if (!d.exists()){
                missingFolders.add(d);
                continue;
            }

            // Set delete variable
            boolean delete = true;

            // Check for filtered folders
            for (String filterFolder : filterFolders) {
                if (d.getAbsolutePath().contains(filterFolder)){
                    delete = false;
                    break;
                }
            }

            // Check if empty
            if (d.listFiles() == null || d.listFiles().length != 0) delete = false;

            // If should be deleted
            if (delete) {

                // Delete file
                if (!d.delete()){

                    // If failed, send warning
                    warn("Tried deleting " + d.getAbsolutePath() + " but failed");

                    // Add to failed list
                    failFolders.add(d);

                } else {

                    // Add file to deleted list
                    delFolders.add(d);
                }

                // If should be saved
            } else {

                // Add file to saved list
                saveFolders.add(d);
            }
        }

        // Print info if requested + failed
        print("DELETED FOLDERS:");
        if (deletedFolders) print(Arrays.toString(delFolders.toArray()));
        print("DELETED FILES:");
        if (deletedFiles) print(Arrays.toString(delFiles.toArray()));
        print("SAVED FOLDERS:");
        if (remainingFolders) print(Arrays.toString(saveFolders.toArray()));
        print("SAVED FILES:");
        if (remainingFiles) print(Arrays.toString(saveFiles.toArray()));
        print("MISSING FOLDERS:");
        print(Arrays.toString(missingFolders.toArray()));
        print("MISSING FILES:");
        print(Arrays.toString(missingFiles.toArray()));
        print("FAILED FOLDERS:");
        print(Arrays.toString(failFolders.toArray()));
        print("FAILED FILES:");
        print(Arrays.toString(failFiles.toArray()));
        printStats(delFileSize, delFiles, delFolders);
    }

    private static void printStats(Long delFileSize, List<File> deletedFiles, List<File> deletedFolders) {
        print("CLEANUP INFO:");
        print(String.format("Deleted %,d MB", delFileSize / 1000000));
        print("over " + deletedFiles.size() + " files.");
        print("over " + deletedFolders.size() + " folders.");
    }

    public static void print(String msg){
        System.out.println("INFO: " + msg);
    }
    public static void warn(String msg){
        System.out.println("WARN: " + msg);
    }
    public static void error(String msg){
        System.out.println("ERROR! " + msg);
    }
}
/*TODO:

class Config {
    private final File configFile;
    private final String path;
    private final Gson gson;
    private final JsonObject config;

    Config(String path, Gson gson){
        this.path = path;
        this.gson = gson;
        this.configFile = new File(path);
        this.config = checkConfig();
    }

    private JsonObject checkConfig() {
        if (!configFile.exists()){
            Cleaner.warn("No config found");
            try {
                if (!configFile.createNewFile()) {
                    Cleaner.error("Failed creating new empty config file");
                } else {
                    try {
                        FileWriter w = new FileWriter(path);
                        gson.toJson(emptyConfig(path), w);
                        w.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Cleaner.error("Failed to write new config to json file");
                    } finally {
                        Cleaner.print("Created new empty config");
                    }
                }
            } catch (IOException e){
                e.printStackTrace();
                Cleaner.error("Failed to create new config file (hard fail)");
            }
        }
        return loadConfig();
    }

    private static Map<String, Object> emptyConfig(String path) {
        Map<String, Object> newConfig = new HashMap<>();
        newConfig.put("serverFolder", path + "server");
        newConfig.put("deletedFiles", true);
        newConfig.put("deletedFolders", true);
        newConfig.put("remainingFiles", true);
        newConfig.put("remainingFolders", true);
        newConfig.put("blacklist", new String[]{"C:\\mustUseFullPath.txt"});
        newConfig.put("files", new String[]{"example.txt"});
        newConfig.put("extensions", new String[]{".txt"});
        newConfig.put("folders", new String[]{"folder"});
        return newConfig;
    }

    private JsonObject loadConfig() {
        try {
            return gson.fromJson(new FileReader(path), JsonObject.class);
        } catch (IOException e){
            e.printStackTrace();
            Cleaner.error("Failed to read from file @ path: " + path);
            return null;
        }
    }

    public JsonObject getConfig() {
        return config;
    }
}
*/