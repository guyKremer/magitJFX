package Engine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Status {
    private String m_repositoryPath;
    private String m_repositoryName;
    private String m_userName;
    private List<String> m_modifiedFiles;
    private List<String> m_addedFiles;
    private List<String> m_deletedFiles;
    private List<String> m_unchangedFiles;
    private Map<String,String> m_unchangedFilesMap;
    private Map<String,String> m_changedFilesMap;

    public Status(String i_repoPath,String i_repoName,String i_userName,List<String> i_modifiedFiles,
                  List<String> i_addedFiles, List<String> i_deletedFiles, List<String> i_unchangedFiles)
    {
        m_repositoryPath = i_repoPath;
        m_repositoryName = i_repoName;
        m_userName = i_userName;

        setModifiedFiles(i_modifiedFiles);
        setAddedFiles(i_addedFiles);
        setDeletedFiles(i_deletedFiles);
        setUnchangedFiles(i_unchangedFiles);
        m_unchangedFilesMap = createMapOfUnChangedFiles();
        m_changedFilesMap = createMapOfChangedFiles();
    }

    private void setUnchangedFiles(List<String> i_unchangedFiles) {
        if(i_unchangedFiles == null){
            m_unchangedFiles = new ArrayList<String>();
        }
        else{
            m_unchangedFiles = i_unchangedFiles;
        }
    }

    private void setModifiedFiles(List<String> i_modifiedFiles) {
        if(i_modifiedFiles == null){
            m_modifiedFiles = new ArrayList<String>();
        }
        else{
            m_modifiedFiles=i_modifiedFiles;
        }
    }
    private void setAddedFiles(List<String> i_addedFiles) {
        if(i_addedFiles == null){
            m_addedFiles = new ArrayList<String>();
        }
        else{
            m_addedFiles = i_addedFiles;
        }
    }
    private void setDeletedFiles(List<String> i_deletedFiles) {
        if(i_deletedFiles == null){
            m_deletedFiles = new ArrayList<String>();
        }
        else{
            m_deletedFiles=i_deletedFiles;
        }
    }

    public String getRepositoryPath(){
        return m_repositoryPath;
    }
    public String getRepositoryName(){
        return m_repositoryName;
    }
    public String getUserName() {
        return m_userName;
    }
    public List<String> getModifiedFiles(){
        return m_modifiedFiles;
    }
    public List<String> getAddedFiles(){
        return m_addedFiles;
    }
    public  Map<String,String> getUnchangedFilesMap(){return m_unchangedFilesMap;}
    public  Map<String,String> getChangedFilesMap(){return m_changedFilesMap;}

    public  Map<String,String> createMapOfUnChangedFiles(){
        Map<String,String> res= new HashMap<>();
        if(!m_unchangedFiles.isEmpty()){
            for(String str: m_unchangedFiles){
                res.put(str,"UNCHANGED");
            }
        }

        return res;
    }

    public Map<String,String> createMapOfChangedFiles(){
        Map<String,String> res= new HashMap<>();

        if(!m_addedFiles.isEmpty()){
            for(String str: m_addedFiles){
                res.put(str,"ADDED");
            }
        }
        if(!m_modifiedFiles.isEmpty()){
            for(String str: m_modifiedFiles){
                res.put(str,"MODIFIED");
            }
        }
        if(!m_deletedFiles.isEmpty()){
            for(String str: m_deletedFiles){
                res.put(str,"DELETED");
            }
        }

        return res;
    }
    public List<String> getDeletedFiles(){
        return m_deletedFiles;
    }
}
