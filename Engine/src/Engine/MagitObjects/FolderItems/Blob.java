package Engine.MagitObjects.FolderItems;

import Engine.Engine;
import Engine.MagitObjects.Commit;
import Engine.MagitObjects.Repository;
import org.apache.commons.codec.digest.DigestUtils;
import Engine.Status;
import org.apache.commons.io.FileUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Blob extends FolderItem {
    String m_content;
    public Blob(Path i_blobPath,String i_content) {
        super(i_blobPath);
        m_type = "file";
        m_content = i_content;
        createSha1();
    }

    public Blob(){}

    public String GetContent() {
        return m_content;
    }

    @Override
    public void saveInObjects() {
        try{
            Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(m_sha1)
                                    ,m_content);
        }
        catch (java.io.IOException e){

        }
    }

   public void unzipAndSaveFile(Path i_path,String[] i_blobDesription){
        File tempBlob;

        try{
            tempBlob =Engine.Utils.UnzipFile(i_blobDesription[1]);
            m_path = i_path;
            m_content = new String(Files.readAllBytes(tempBlob.toPath()));
            tempBlob.delete();
            m_type = "file";
            m_name = i_blobDesription[0];
            m_sha1 = i_blobDesription[1];
            m_updater= i_blobDesription[3];
            m_lastModified = i_blobDesription[4];
        }
        catch( java.io.IOException e){

        }
   }

    @Override
    protected void UpdateChangedFolderItemsRec(Status i_status, Map<Path, FolderItem> i_map){
        updateAuthorAndLastModifiedIfNeeded(i_status,i_map);
    }
    @Override
    public void createSha1() {
        m_sha1 = DigestUtils.sha1Hex(m_content);
    }

    @Override
    public void flushToWc(){
        try{
            File blob;
            if(!Files.exists(m_path)){
                 blob = Files.createFile(m_path).toFile();
            }
            else{
                blob = m_path.toFile();
            }
            FileUtils.writeStringToFile(blob,m_content, Charset.forName("utf-8"),false);
        }
        catch(java.io.IOException e){

        }
    }

    @Override
    public void flushForMergeToWc(Commit ancestor, Commit ours){
        String content;
        try{
            File blob;
            if(!Files.exists(m_path)){
                blob = Files.createFile(m_path).toFile();
                content = m_content;
            }
            else{
                Blob ancestorFile = (Blob)ancestor.getRootFolder().GetItem(m_path);
                Blob oursFile = (Blob)ours.getRootFolder().GetItem(m_path);
                if(ancestorFile!=null){
                    if(oursFile.m_sha1.equals(ancestorFile.m_sha1)){
                        content = m_content;
                    }
                    else{
                        content = oursFile.m_content;
                    }
                }
                else {
                    content = oursFile.m_content;
                }
                blob = m_path.toFile();
            }
            FileUtils.writeStringToFile(blob,content, Charset.forName("utf-8"),false);
        }
        catch(java.io.IOException e){

        }
    }

    @Override
    public void fullToString(List<String> i_str){
        StringBuilder str = new StringBuilder();
        str.append(m_path + ",").append(toString());
        i_str.add(str.toString());
    }

}

