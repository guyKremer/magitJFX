package Engine.MagitObjects.FolderItems;

import Engine.Engine;
import Engine.MagitObjects.Commit;
import Engine.MagitObjects.Repository;
import org.apache.commons.codec.digest.DigestUtils;
import Engine.Status;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import Engine.Status;
import org.apache.commons.io.FileUtils;


public class Folder extends FolderItem{
    private List<FolderItem> m_items = new ArrayList<>();

    public Folder(Path i_folderPath){
        super(i_folderPath);
        m_type="folder";
    }

    public List<FolderItem> GetItems() {
        return m_items;
    }

    public Folder(Path i_path, String[] i_fileDescription){
        super(i_path);
        m_sha1 = i_fileDescription[1];
        m_type="folder";
        m_updater=i_fileDescription[3];
        m_lastModified= i_fileDescription[4];
    }

    public void InsertItem(FolderItem i_item){
        m_items.add(i_item);
        Collections.sort(m_items);
    }

    public Folder(Path i_folderPath,String i_sha1,String i_updater, String i_lastModified){
        super(i_folderPath);
        m_type="folder";
        m_sha1=i_sha1;
        m_updater=i_updater;
        m_lastModified=i_lastModified;
    }

    public Path getPath(){
        return m_path;
    }

    public Folder(){}

    public void loadFolder() throws IOException {
        Blob blobToInsert;
        Folder folderToInsert;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(m_path)){
            for(Path entry: stream){
                if(!Files.isDirectory(entry)){
                    blobToInsert=new Blob(entry,new String(Files.readAllBytes(entry)));
                    InsertItem(blobToInsert);
                }
                else{
                    if(!(entry.getFileName().toString().equals(".magit"))){
                        folderToInsert=new Folder(entry);
                        folderToInsert.loadFolder();
                        if(folderToInsert.m_items.size()!=0){
                            InsertItem(folderToInsert);
                        }
                    }
                }
            }
            createSha1();
        }
    }

    @Override
    public void createSha1(){
        StringBuilder listContent= new StringBuilder();

        for(FolderItem item: m_items){
            listContent.append(item.m_name + item.m_sha1 + item.m_type);
        }
        m_sha1 = DigestUtils.sha1Hex(listContent.toString());
    }

    public String getSha1(){
        return m_sha1;
    }

    @Override
    public void saveInObjects(){
        for (FolderItem item: m_items){
            item.saveInObjects();
        }
        try{
            /*
            if(hasChanged(i_status));{

            }
            */

            Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(m_sha1)
                                    ,createListString());
        }
        catch(java.io.IOException e){

        }
    }
/*
    private boolean hasChanged(Status i_status) {

    }
*/
    public String createListString(){
        StringBuilder itemListAsString = new StringBuilder();
        for(FolderItem item: m_items){
            itemListAsString.append(item.toString());
            itemListAsString.append(System.getProperty("line.separator"));
        }
        return itemListAsString.toString();
    }

    @Override
    public void flushToWc(){
        try{
            for (FolderItem item: m_items){
                if(!Files.exists(m_path)){
                    Files.createDirectories(m_path);
                }
                item.flushToWc();
            }
        }
        catch(java.io.IOException e){

        }
    }
    @Override
    public void flushForMergeToWc(Commit ancestor, Commit ours){
        try{
            for (FolderItem item: m_items){
                if(!Files.exists(m_path)){
                    Files.createDirectories(m_path);
                }
                item.flushForMergeToWc(ancestor,ours);
            }
        }
        catch(java.io.IOException e){

        }
    }

    public void unzipAndSaveFolder(String i_directorySha1){
        try{
            Blob blobToInsert;
            Folder folderToInsert;
            String[] parsedFolderLine;
            File tempFile = Engine.Utils.UnzipFile(i_directorySha1);
            List<String> lines = Files.readAllLines(tempFile.toPath());
            tempFile.delete();
            for(String line: lines){
                parsedFolderLine=parseFolderLine(line);
                if(parsedFolderLine[2].equals("file")){
                    blobToInsert = new Blob();
                    blobToInsert.unzipAndSaveFile(m_path.resolve(parsedFolderLine[0]),parsedFolderLine);
                    InsertItem(blobToInsert);
                }
                else{
                    folderToInsert=new Folder(m_path.resolve(parsedFolderLine[0]),parsedFolderLine);
                    folderToInsert.unzipAndSaveFolder(folderToInsert.m_sha1);
                    InsertItem(folderToInsert);
                }
            }
            createSha1();
        }
        catch (java.io.IOException e){

        }
    }


    public String[] parseFolderLine(String i_line){
        String[] parsedLine = i_line.split(",");
        return parsedLine;
    }

    public void createPathToSha1Map(Map<Path, String> i_res) {
        i_res.put(m_path,m_sha1);
        for (FolderItem item : m_items) {
            if (item.m_type.equals("folder")) {
                ((Folder) item).createPathToSha1Map(i_res);
            }
            else{
                i_res.put(item.m_path, item.m_sha1);
            }
        }
    }

    public void createPathToItemMap(Map<Path, FolderItem> i_res){
        for (FolderItem item : m_items) {
            if (item.m_type.equals("folder")) {
                ((Folder) item).createPathToItemMap(i_res);
            }
            i_res.put(item.m_path, item);
        }
    }
    @Override
    public void fullToString(List<String> i_res){
        StringBuilder str = new StringBuilder();
        str.append(m_path + ",").append(toString());
        i_res.add(str.toString());
        for (FolderItem item: m_items){
            item.fullToString(i_res);
        }
    }

    @Override
    protected void UpdateChangedFolderItemsRec(Status i_status,Map<Path, FolderItem> i_map) {
        for(FolderItem item: m_items){
            item.UpdateChangedFolderItemsRec(i_status,i_map);
        }
        updateAuthorAndLastModifiedIfNeeded(i_status,i_map);
    }


    public void UpdateChangedFolderItems(Status i_status,Map<Path, FolderItem> i_map) {
        UpdateChangedFolderItemsRec(i_status,i_map);
    }


    public FolderItem GetItem(String sha1) {
        FolderItem res=null;
        for (FolderItem item : m_items) {
            if(sha1.equals(item.m_sha1)){
                res = item;
                break;
            }
            else if (item.m_type.equals("folder")) {
                res = ((Folder) item).GetItem(sha1);
                if(res != null){
                    break;
                }
            }
        }
        return res;
    }

    public FolderItem GetItem(Path path) {
        FolderItem res=null;
        for (FolderItem item : m_items) {
            if(path.toString().equals(item.m_path.toString())){
                res = item;
                break;
            }
            else if (item.m_type.equals("folder")) {
                res = ((Folder) item).GetItem(path);
                if(res != null){
                    break;
                }
            }
        }
        return res;
    }

    public void initFolderPaths(Path i_NewPathOfRepository) {
        this.m_path = i_NewPathOfRepository;

        for (FolderItem item : this.m_items) {
            if (item.m_type.equals("folder")) {
                Folder currentFolder = (Folder) item;
                currentFolder.initFolderPaths(i_NewPathOfRepository.resolve(currentFolder.GetName()));
            } else
                item.setPath(i_NewPathOfRepository.resolve(item.GetName()));
        }
    }
}
