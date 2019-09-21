package Engine.MagitObjects;

import Engine.Engine;
import Engine.MagitObjects.FolderItems.Blob;
import Engine.MagitObjects.FolderItems.Folder;
import Engine.MagitObjects.FolderItems.FolderItem;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import org.apache.commons.io.FileUtils;
import Engine.Status;


import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Repository {
    private String m_name;
    public static Path m_repositoryPath=null;
    public static Path m_pathToMagitDirectory=null;
    public static SimpleDateFormat m_simpleDateFormat = null;
    private Map<String,Branch> m_branches = new HashMap<String, Branch>();
    private Branch m_headBranch;
    private Commit m_currentCommit=null;
    private Folder m_WC;

    // getters


    public String GetName() {
        return m_name;
    }

    public Path GetRepositoryPath() {
        return m_repositoryPath;
    }

    public Path GetPathToMagitDirectory() {
        return m_pathToMagitDirectory;
    }

    public Map<String, Branch> GetBranches() {
        return m_branches;
    }

    public Branch GetHeadBranch() {
        return m_headBranch;
    }

    public Commit GeCurrentCommit() {
        return m_currentCommit;
    }

    public Folder GetWC() {
        return m_WC;
    }

    //

    public void SetCommitFromBranch(Branch i_branch,boolean flush)throws IOException{
        loadCommitFromBranch(i_branch);
        if(flush){
            flushCommit();
        }
    }

    public boolean isFirstCommitExist(){
        return (m_currentCommit != null);
    }

    public Repository(String i_name,String i_repository, boolean i_exists)throws java.io.IOException{
        m_name = i_name;
        m_repositoryPath=Paths.get(i_repository);
        m_simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS");
        m_branches = new HashMap<String, Branch>();
        m_pathToMagitDirectory = m_repositoryPath.resolve(".magit");
        m_WC = new Folder(m_repositoryPath);
        if(!i_exists){
            initializeRepository();
        }
        else{
            switchRepository();
        }
    }

    public void InsertBranch(Branch i_branch){
        m_branches.put(i_branch.getName(),i_branch);
    }

    public void SetHeadBranch(Branch i_branch) throws IOException {
        m_headBranch = i_branch;
        Path headPath=m_pathToMagitDirectory.resolve("branches").resolve("HEAD");
        //switchHeadBranchInList(i_branch);
        FileUtils.writeStringToFile(headPath.toFile(), m_headBranch.getName(), Charset.forName("utf-8"),false);
    }

    private void initializeRepository()throws java.io.IOException{
        Path pathToObjectsDirectory = m_pathToMagitDirectory.resolve("objects");
        Path pathToBranchDirectory = m_pathToMagitDirectory.resolve("branches");
        Files.createDirectories(m_pathToMagitDirectory);
        Files.createFile(m_pathToMagitDirectory.resolve("RepoName"));
        FileUtils.writeStringToFile(m_pathToMagitDirectory.resolve("RepoName").toFile(), m_name, Charset.forName("utf-8"),false);
        Files.createDirectories(pathToObjectsDirectory);
        Files.createDirectories(pathToBranchDirectory);
        Files.createFile(pathToBranchDirectory.resolve("HEAD"));
        FileUtils.writeStringToFile(pathToBranchDirectory.resolve("HEAD").toFile(), "master", Charset.forName("utf-8"),false);
        Path masterPath = pathToBranchDirectory.resolve("master");
        Files.createFile(masterPath);
        Branch master = new Branch(masterPath,"");
        m_branches.put(master.getName(),master);
        SetHeadBranch(master);
    }

    public void flushBranches() throws IOException {
        for ( String branchName : m_branches.keySet()){
            m_branches.get(branchName).flushBranch();
        }
    }

    public List<Commit> GetHeadBranchCommitHistory()throws FileNotFoundException,IOException {
        if(m_currentCommit==null){
            throw new FileNotFoundException("There are no commits in the current branch");
        }
        List<Commit> res = new ArrayList<>();
        GetHeadBranchCommitHistoryRec(res,m_headBranch.getCommitSha1());
        return res;
    }

    private void GetHeadBranchCommitHistoryRec(List<Commit> i_res,String i_sha1)throws IOException {
        Commit commitToInsert = new Commit(i_sha1);
        i_res.add(commitToInsert);
        if(!commitToInsert.getFirstPrecedingSha1().isEmpty()){
            GetHeadBranchCommitHistoryRec(i_res,commitToInsert.getFirstPrecedingSha1());
        }
        else if(!commitToInsert.getSecondPrecedingSha1().isEmpty()){
            GetHeadBranchCommitHistoryRec(i_res,commitToInsert.getSecondPrecedingSha1());
        }
    }


    public Folder loadWC() throws IOException {
        Folder res=null;
        if(m_repositoryPath != null) {
            res = new Folder(m_repositoryPath);
            res.loadFolder();
        }
        return res;
    }

    public void createCommit(String i_message)throws FileAlreadyExistsException,java.io.IOException {

        Map<String,List<String>> changesMap = checkChanges();
        Status status = new Status(m_repositoryPath.toString(),m_name, Engine.m_user,
                changesMap.get("MODIFIED"), changesMap.get("ADDED"), changesMap.get("DELETED"),changesMap.get("UNCHANGED"));

        if (status.getModifiedFiles().isEmpty() && status.getAddedFiles().isEmpty()
                && status.getDeletedFiles().isEmpty())
        {
            throw new FileAlreadyExistsException ("There are no changes to commit");
        }

        m_WC=loadWC();
        if(m_WC.GetItems().size()==0){
            throw new FileNotFoundException("You can't commit an empty folder");
        }
         Map<Path,FolderItem> pathToItemMap = new HashMap<>();
         if(m_currentCommit !=null){
             m_currentCommit.getRootFolder().createPathToItemMap(pathToItemMap);
         }
        m_WC.UpdateChangedFolderItems(status,pathToItemMap);
         if(m_currentCommit!=null){
             m_currentCommit = new Commit(i_message,m_WC,m_currentCommit.getFirstPrecedingSha1(),
                     m_currentCommit.getSecondPrecedingSha1()
                     ,m_simpleDateFormat.format(new Date()),Engine.m_user);
         }
         else{
             m_currentCommit = new Commit(i_message,m_WC,"",""
                     ,m_simpleDateFormat.format(new Date()),Engine.m_user);
         }

        m_headBranch.setCommitSha1(m_currentCommit.getSha1());
        m_headBranch.flushBranch();
        Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(m_currentCommit.getSha1())
                ,m_currentCommit.toString());
    }

    public void loadCommitFromBranch(Branch i_branch)throws java.io.IOException{
        m_currentCommit = new Commit (i_branch.getCommitSha1());
        m_WC = m_currentCommit.getRootFolder();
    }

    public void clearWc(){
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(m_WC.getPath())){
            for(Path entry: stream){
                if(!Files.isDirectory(entry)){
                    new File(entry.toString()).delete();
                }
                else{
                    if(!(entry.getFileName().toString().equals(".magit"))){
                        FileUtils.deleteDirectory(new File(entry.toString()));
                    }
                }
            }
        }
        catch(java.io.IOException e){

        }
    }

    public void AddBranch(String i_branchName,boolean i_checkout)throws FileAlreadyExistsException,IOException{
        Path pathToBranch = m_pathToMagitDirectory.resolve("branches").resolve(i_branchName);
        if(Files.exists(pathToBranch)){
            throw new FileAlreadyExistsException("The branch " + i_branchName +" already exists");
        }
        else{
            m_branches.put(i_branchName, new Branch(pathToBranch,m_headBranch.getCommitSha1()));
        }
        if(i_checkout){
            checkOut(i_branchName);
        }
    }

    public Map<String,List<String>> checkChanges() throws IOException {
        Map<String,List<String>> res =  new HashMap<>();
        Map<Path,String> commitPathToSha1Map= new HashMap<>();
        Map<Path,String> WCPathToSha1Map= new HashMap<>();
        String sha1;
        m_WC = loadWC();
        if(m_currentCommit!=null){
            m_currentCommit.getRootFolder().createPathToSha1Map(commitPathToSha1Map);
        }
        m_WC.createPathToSha1Map(WCPathToSha1Map);
        List<Path> pathsToRemove = new ArrayList();

                for (Path path : WCPathToSha1Map.keySet()) {
                    sha1 = commitPathToSha1Map.get(path);
                    if (sha1 != null) { // checks if key exists
                        if (!WCPathToSha1Map.get(path).equals(sha1)) {
                            updateChangesMap(res, "MODIFIED", path.toString());
                        }
                        else{
                            updateChangesMap(res,"UNCHANGED",path.toString());
                        }
                        pathsToRemove.add(path);
                    }
                }

                for (Path path : pathsToRemove) {
                    WCPathToSha1Map.remove(path);
                    commitPathToSha1Map.remove(path);
                }

                for (Path path : WCPathToSha1Map.keySet()) {
                    updateChangesMap(res, "ADDED", path.toString());

                }
                for (Path path : commitPathToSha1Map.keySet()) {
                    updateChangesMap(res, "DELETED", path.toString());
                }

        return res;
    }

    private void updateChangesMap( Map<String,List<String>> i_map,String i_key,String i_value){
        List<String> pathList;

        if(!i_map.containsKey(i_key)) {
            i_map.put(i_key, new ArrayList<>());
        }
            pathList =i_map.get(i_key);
            pathList.add(i_value);
            i_map.put(i_key,pathList);
    }

    public void readPartialCommitLines(List<String> i_lines,Commit i_commit){

        for (int i=0; i < i_lines.size(); i++) {
            switch(i){
                case 1:i_commit.setFirstPrecedingSha1(i_lines.get(i));
                    break;
                case 2:i_commit.setSecondPrecedingSha1(i_lines.get(i));
                    break;

                case 3:i_commit.setM_message(i_lines.get(i));
                    break;

                case 4: i_commit.setM_dateOfCreation(i_lines.get(i));
                    break;

                case 5:i_commit.setM_creator(i_lines.get(i));
            }
        }
    }

    private void switchRepository()throws java.io.IOException {
        loadBranches();
        setHeadBranchFromHead();
        if(!m_headBranch.getCommitSha1().equals("")){
            m_currentCommit = new Commit(m_headBranch.getCommitSha1());
            flushCommit();
        }
        else{
            clearWc();
            m_currentCommit=null;
        }
    }

    private void loadBranches()throws IOException {
        Path pathToBranchesDirectory = m_pathToMagitDirectory.resolve("branches");
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(pathToBranchesDirectory)){
            for(Path entry: stream) {
                if(!entry.getFileName().toString().equals("HEAD")){
                    m_branches.put(entry.getFileName().toString(),new Branch(entry,FileUtils.readFileToString(entry.toFile(), Charset.forName("utf-8"))));
                }
            }
        }
    }

    private void setHeadBranchFromHead()throws FileNotFoundException, java.io.IOException {
        Path pathToHead = m_pathToMagitDirectory.resolve("branches").resolve("HEAD");
        String branchName = FileUtils.readFileToString(pathToHead.toFile(), Charset.forName("utf-8"));

        Path pathToHeadBranch = m_pathToMagitDirectory.resolve("branches").resolve(branchName);
        if(!Files.exists(pathToHeadBranch)){
            throw new FileNotFoundException(pathToHeadBranch.toString() + "Head branch points to a non existing branch ");
        }
        String commitSha1 = FileUtils.readFileToString(pathToHeadBranch.toFile(), Charset.forName("utf-8"));
        SetHeadBranch(m_branches.get(pathToHeadBranch.getFileName().toString()));
    }

    public List<String> showCurrentCommitFiles()throws NullPointerException {
        List<String> res = new ArrayList<>();
        if(m_currentCommit == null){
            throw new NullPointerException("Nothing was committed, add a commit first");
        }
         m_currentCommit.getRootFolder().fullToString(res);
        return res;
    }


    public void DeleteBranch(String i_branchName)throws FileNotFoundException,FileAlreadyExistsException,IOException {
        if(m_headBranch.getName().equals(i_branchName)){
            throw new FileAlreadyExistsException(i_branchName + " is the head branch, and cannot be deleted");
        }
        if(m_branches.get(i_branchName) == null){
            throw new FileNotFoundException("The branch " +i_branchName + " doesn't exists");
        }
        else{
            Files.deleteIfExists(m_branches.get(i_branchName).getPathToBranch());
            m_branches.remove(i_branchName);
        }
    }

    public void checkOut(String i_newHeadBranch)throws FileNotFoundException,IOException {
        if(!m_branches.containsKey(i_newHeadBranch)){
            throw new FileNotFoundException(i_newHeadBranch + " is not a branch");
        }
        else{
            SetHeadBranch(m_branches.get(i_newHeadBranch));
            loadCommitFromBranch(m_headBranch);
            flushCommit();
        }
    }

    private void flushCommit(){
        clearWc();
        m_currentCommit.flush();
    }

    public void resetBranchSha1(String i_branchName, String i_sha1)throws FileNotFoundException,IOException {
        if (!m_branches.containsKey(i_branchName)){
            throw new FileNotFoundException("the branch "+ i_branchName + " doesn't exist");
        }
        else{
            Branch branch = m_branches.get(i_branchName);
            branch.setCommitSha1(i_sha1);
            branch.flushBranch();
            m_branches.put(i_branchName,branch);
        }
    }

}