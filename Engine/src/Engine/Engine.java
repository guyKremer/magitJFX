package Engine;

import Engine.MagitObjects.*;
import Engine.MagitObjects.Branch;
import Engine.MagitObjects.Commit;
import Engine.MagitObjects.FolderItems.Folder;
import Engine.MagitObjects.Repository;

import org.apache.commons.io.FileUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;
import puk.team.course.magit.ancestor.finder.MappingFunctionFailureException;
import sun.misc.resources.Messages_de;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Engine {

    private Repository m_currentRepository;
    public static String m_user;

    public Engine(){
        m_user="Administrator";
    }
    public Engine(Repository i_repo,String i_userName) {
        m_currentRepository = i_repo;
        m_user=i_userName;
    }

    public Repository GetCurrentRepository(){
        return m_currentRepository;
    }

    public void initializeRepository(String i_pathToRepo, String i_repoName)throws FileAlreadyExistsException,java.io.IOException{
        Path path = Paths.get(i_pathToRepo);

        if(!Files.exists(path)){
            Files.createDirectories(path);
        }
        if(!Files.exists(path.resolve(".magit"))){
            m_currentRepository=new Repository(i_repoName,i_pathToRepo,false);
        }
        else{
            throw new FileAlreadyExistsException(i_pathToRepo + " is already a repository ");
        }
    }

    public void createNewCommit(String i_message)throws FileAlreadyExistsException,java.io.IOException {
        isRepositoryInitialized();
        m_currentRepository.createCommit(i_message);
    }

    public boolean isFirstCommitExist(){
        return m_currentRepository.isFirstCommitExist();
    }

    public Status showStatus()throws java.io.IOException{
        isRepositoryInitialized();
        if(!isFirstCommitExist()){
            throw new FileNotFoundException("Cant show status because  nothing was committed");
        }
        Map<String,List<String>> changesMap = m_currentRepository.checkChanges();
        Status res;
        res = new Status(m_currentRepository.m_repositoryPath.toString(),m_currentRepository.GetName(), m_user,
                changesMap.get("MODIFIED"), changesMap.get("ADDED"), changesMap.get("DELETED"),changesMap.get("UNCHANGED"));

        return res;
    }

    public void isRepositoryInitialized() {
        if(m_currentRepository == null){
            throw new NullPointerException("No repository was initialized");
        }
    }

    public void switchRepository(String i_pathToRepo)throws FileNotFoundException,java.io.IOException{
        Path path = Paths.get(i_pathToRepo);
        if(!Files.exists(path.resolve(".magit"))){
            throw new FileNotFoundException(i_pathToRepo + " is not a repository");
        }
        else{
            List<String> lines = Files.readAllLines(Paths.get(i_pathToRepo).resolve(".magit").resolve("RepoName"));
            if(lines.size() == 1) {
                m_currentRepository = new Repository(lines.get(0), i_pathToRepo, true);
            }
            else{
                m_currentRepository = new LocalRepository(lines.get(0), i_pathToRepo, true, lines.get(1), lines.get(2));
            }
        }
    }

    public void DeleteBranch(String i_branchName) throws FileNotFoundException,FileAlreadyExistsException,IOException {
        isRepositoryInitialized();
        m_currentRepository.DeleteBranch(i_branchName);
    }

    public void checkOut(String i_newHeadBranch)throws FileNotFoundException,IOException {
        isRepositoryInitialized();
        m_currentRepository.checkOut(i_newHeadBranch);
    }

    public void resetBranchSha1(String i_sha1)throws FileNotFoundException,IOException {
        isRepositoryInitialized();
        m_currentRepository.resetBranchSha1(m_currentRepository.GetHeadBranch().getName(),i_sha1);
    }

    public void setCurrentRepository(Repository repo){
        this.m_currentRepository=repo;
    }


    public List<Commit> GetHeadBranchCommitHistory()throws FileNotFoundException,IOException{
        isRepositoryInitialized();
        return m_currentRepository.GetHeadBranchCommitHistory();
    }

    public Branch GetHeadBranch() {
        isRepositoryInitialized();
        return m_currentRepository.GetHeadBranch();
    }

    public int needFastForwardMerge(String theirsBranchName)throws FileNotFoundException,IOException {
        isRepositoryInitialized();
        Branch theirsBranch = m_currentRepository.GetBranch(theirsBranchName);
        if(theirsBranch==null){
            throw new FileNotFoundException(theirsBranchName+ " isn't a branch");
        }
        return m_currentRepository.needFastForwardMerge(m_currentRepository.GetBranch(theirsBranchName));
    }

    public void forwardMerge(String theirsBranchName)throws FileNotFoundException,IOException{
        isRepositoryInitialized();
        isOpenChanges();
        Branch theirsBranch = m_currentRepository.GetBranch(theirsBranchName);
        if(theirsBranch==null){
            throw new FileNotFoundException(theirsBranchName+ " isn't a branch");
        }
        m_currentRepository.forwardMerge(theirsBranchName);
    }

    public Status showStatusAgainstOtherCommits(Commit commit, String prevCommitSha1)throws IOException {
        isRepositoryInitialized();
        Commit originalCommit = m_currentRepository.GeCurrentCommit();
        Folder originalWc = m_currentRepository.loadWC();
        Status status=new Status(m_currentRepository.m_repositoryPath.toString(),m_currentRepository.GetName(),m_user,null,null,null,null);

        if(prevCommitSha1!=null && !prevCommitSha1.isEmpty()){
            m_currentRepository.clearWc();
            Commit prevCommit = new Commit(prevCommitSha1);
            m_currentRepository.setCurrentCommit(prevCommit);
            originalCommit.flush();
            status = showStatus();
        }
        m_currentRepository.setCurrentCommit(originalCommit);
        m_currentRepository.clearWc();
        originalWc.flushToWc();
        return status;
    }

    public static class Utils{
        public static void zipToFile(Path i_pathToZippedFile, String i_fileContent) throws IOException{
            if(!Files.exists(i_pathToZippedFile)){
                Path pathToTempSha1 = Repository.m_repositoryPath.resolve(i_pathToZippedFile.getFileName());//tempFile
                FileOutputStream fos = new FileOutputStream(i_pathToZippedFile.toString());
                ZipOutputStream zipOut = new ZipOutputStream(fos);
                File fileToZip=Files.createFile(pathToTempSha1).toFile();

                try (FileWriter writer = new FileWriter(fileToZip.toString());
                     BufferedWriter bw = new BufferedWriter(writer)){
                    bw.write(i_fileContent);
                }

                FileInputStream fis = new FileInputStream(fileToZip);
                ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
                zipOut.putNextEntry(zipEntry);
                byte[] bytes = new byte[1024];
                int length;
                while((length = fis.read(bytes)) >= 0) {
                    zipOut.write(bytes, 0, length);
                }
                zipOut.close();
                fis.close();
                fos.close();
                Files.deleteIfExists(pathToTempSha1);
            }
        }
        public static File UnzipFile(String i_sha1)throws IOException{
            Path fileZip = Repository.m_pathToMagitDirectory.resolve("objects").resolve(i_sha1);
            //Files.createDirectories(Repository.m_pathToMagitDirectory.resolve("temp"));
            File destDir = new File(Repository.m_pathToMagitDirectory.toString());
            byte[] buffer = new byte[1024];
            ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip.toFile()));
            ZipEntry zipEntry = zis.getNextEntry();
            File newFile =null;
            while (zipEntry != null) {
                    newFile = newFile(destDir, zipEntry);
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();

            return newFile;
        }
    }

    public static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();

        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }

        return destFile;
    }


    public List<String> showCurrentCommitFiles()throws NullPointerException{
        isRepositoryInitialized();
        return m_currentRepository.showCurrentCommitFiles();
    }

    public Map<String, Branch> GetRepoBranches(){
        isRepositoryInitialized();
        return m_currentRepository.GetBranches();
    }

    public void AddBranch(String i_branchName,boolean i_checkout)throws FileAlreadyExistsException,IOException{
        isRepositoryInitialized();
        isOpenChanges();
        m_currentRepository.AddBranch(i_branchName,i_checkout);
    }

    private void isOpenChanges() throws FileNotFoundException,IOException{
        Status status = showStatus();
        if (!status.getModifiedFiles().isEmpty() || !status.getAddedFiles().isEmpty()
                || !status.getDeletedFiles().isEmpty()){
            throw new FileNotFoundException("Cant perform action because You have open changes");
        }
    }

    public Map<Path,Conflict> Merge(String i_theirs,boolean checkConflicts)throws FileNotFoundException,IOException{
        isRepositoryInitialized();
        isOpenChanges();
        Branch theirsBranch =  m_currentRepository.GetBranch(i_theirs);

        //if branch exists
        if(theirsBranch!= null){
            return  m_currentRepository.Merge(theirsBranch,checkConflicts);
        }
        else{
            throw new FileNotFoundException(i_theirs+ " isn't a branch");
        }
    }

    public Map<Path,Conflict> CheckConflicts(String  i_theirsBranchName)throws FileNotFoundException,IOException{
        Branch theirsBranch = m_currentRepository.GetBranches().get(i_theirsBranchName);
        if(theirsBranch!=null){
            return  m_currentRepository.checkConflicts(theirsBranch);
        }
        else {
            throw new FileNotFoundException("The branch " + i_theirsBranchName + "doesnt exists");
        }
    }

    public void Clone(File i_RR, File i_LR, String repoName) throws IOException {
        String RR = i_RR.getAbsolutePath();
        RBranch rb;
        RTBranch rtBranch;
        switchRepository(RR);
        Map<String,Branch> branches = m_currentRepository.GetBranches();
        Map<String,Commit> commitsMap = m_currentRepository.GetCommitsMap();
        LocalRepository LR = new LocalRepository(repoName,
                i_LR.getAbsolutePath(),
                false,
                RR,
                m_currentRepository.GetName());

        LR.SetCommitsMap(commitsMap);

        FileUtils.deleteQuietly(LR.GetPathToMagitDirectory().resolve("branches").resolve("master").toFile());

        initNewPaths(LR.GetRepositoryPath(), LR);

        for(Commit commit : LR.GetCommitsMapObj().values()){
            commit.getRootFolder().saveInObjects();
            Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(commit.getSha1())
                    , commit.toString());
        }


        for (Branch branch : m_currentRepository.GetBranches().values()) {
            rb = new RBranch(LR.GetRepositoryPath().resolve(".magit").resolve("branches").resolve(m_currentRepository.GetName())
                    .resolve(branch.getName()),
                    m_currentRepository.GetName() + File.separator + branch.getName(),
                    branch.getCommitSha1());
            LR.InsertBranch(rb);
        }

        rtBranch = new RTBranch(LR.GetRepositoryPath().resolve(".magit").resolve("branches").
                resolve(m_currentRepository.GetHeadBranch().getName()), m_currentRepository.GetHeadBranch().getCommitSha1());


        LR.InsertBranch(rtBranch);
        LR.SetHeadBranch(m_currentRepository.GetHeadBranch());
        m_currentRepository = LR;

        checkOut(m_currentRepository.GetHeadBranch().getName());

    }

    private void initNewPaths(Path i_NewPathOfRepository, Repository i_repo) throws IOException {
        for (Commit currentCommit : i_repo.GetCommitsMapObj().values())
        {
            currentCommit.getRootFolder().initFolderPaths(i_NewPathOfRepository);
        }
    }

    public void Fetch() throws IOException {

        Path currMagitPath = Repository.m_pathToMagitDirectory;
        Path currRepoPath = Repository.m_repositoryPath;

        Repository RR = new Repository(((LocalRepository)m_currentRepository).getRemoteRepoName(),
                ((LocalRepository)m_currentRepository).getRemoteRepoLocation(), true);


        Map<String, Branch> newBranches = new HashMap<>();
        Branch tempBranch;
        RBranch rb;

        RR.GetCommitsMap();

        Repository.m_pathToMagitDirectory = currMagitPath;
        Repository.m_repositoryPath = currRepoPath;

        initNewPaths(m_currentRepository.GetRepositoryPath(), RR);

        for(Commit commit : RR.GetCommitsMapObj().values()){
            commit.getRootFolder().saveInObjects();
            Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(commit.getSha1())
                    ,commit.toString());
        }


        //build new branches to the LR

        for(Map.Entry<String,Branch> branch: RR.GetBranches().entrySet()){
            if(m_currentRepository.GetBranches().containsKey(RR.GetName() + File.separator + branch.getKey())){
                newBranches.put(
                        RR.GetName() + File.separator + branch.getKey(),
                        new RBranch(Repository.m_pathToMagitDirectory.resolve("branches").resolve(RR.GetName())
                                .resolve(branch.getKey()),
                                RR.GetName() + File.separator + branch.getKey(),
                                branch.getValue().getCommitSha1()));
            }
        }

        for(Map.Entry<String,Branch> branch : m_currentRepository.GetBranches().entrySet()){
            if(!(branch.getValue() instanceof RBranch)){
                newBranches.put(branch.getKey(),branch.getValue());
            }

        }
        m_currentRepository.SetBranches(newBranches);

    }

    public void Pull() throws IOException {

        Path currRepoPath = Repository.m_repositoryPath;
        Path currMagitPath = Repository.m_pathToMagitDirectory;

        Commit branchCommit1;
        Commit branchCommit2 = null;

        Repository RR = new Repository(((LocalRepository)m_currentRepository).getRemoteRepoName(),
                ((LocalRepository)m_currentRepository).getRemoteRepoLocation(), true);

        Branch branch;
        RTBranch newRTBranch;
        RBranch newRBranch;

        RR.GetCommitsMap();

        branchCommit1 = new Commit(RR.GetHeadBranch().getCommitSha1());


        Repository.m_pathToMagitDirectory = currMagitPath;
        Repository.m_repositoryPath = currRepoPath;

        initNewPaths(m_currentRepository.GetRepositoryPath(), RR);

        //update commits of the branch

        while(((branchCommit1.getFirstPrecedingSha1() != null && !branchCommit1.getFirstPrecedingSha1().isEmpty()) ||
                (branchCommit1.getSecondPrecedingSha1() != null && !branchCommit1.getSecondPrecedingSha1().isEmpty()))
        ||(branchCommit2 != null && (
                (branchCommit2.getFirstPrecedingSha1() != null && !branchCommit2.getFirstPrecedingSha1().isEmpty())||
                        branchCommit2.getSecondPrecedingSha1() != null && !branchCommit2.getSecondPrecedingSha1().isEmpty()))){

            if(branchCommit1.getFirstPrecedingSha1() != null && !branchCommit1.getFirstPrecedingSha1().isEmpty() ||
            (branchCommit1.getSecondPrecedingSha1() != null && !branchCommit1.getSecondPrecedingSha1().isEmpty())) {
                branchCommit1.getRootFolder().saveInObjects();
                Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(branchCommit1.getSha1())
                        , branchCommit1.toString());
                branchCommit1 = RR.GetCommitsMapObj().get(branchCommit1.getFirstPrecedingSha1());
                branchCommit2 = RR.GetCommitsMapObj().get(branchCommit1.getSecondPrecedingSha1());

            }else if( branchCommit2 != null &&
                    (branchCommit2.getFirstPrecedingSha1() != null && !branchCommit2.getFirstPrecedingSha1().isEmpty())||
                    branchCommit2.getSecondPrecedingSha1() != null && !branchCommit2.getSecondPrecedingSha1().isEmpty()){
                branchCommit2.getRootFolder().saveInObjects();
                Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(branchCommit2.getSha1())
                        , branchCommit2.toString());
                branchCommit1 = RR.GetCommitsMapObj().get(branchCommit2.getFirstPrecedingSha1());
                branchCommit2 = RR.GetCommitsMapObj().get(branchCommit2.getSecondPrecedingSha1());
            }
        }

        if(m_currentRepository.GetHeadBranch() instanceof RTBranch){
            //CHECK IF NEED PUSH BEFORE
            //*************************

            // update RB in LR
            branch = RR.GetBranches().get(m_currentRepository.GetHeadBranch().getName());
            m_currentRepository.GetBranches().remove(RR.GetName() + File.separator + branch.getName());

            newRBranch = new RBranch(Repository.m_pathToMagitDirectory.resolve("branches").resolve(RR.GetName()).resolve(branch.getName()),
                    RR.GetName() + File.separator + branch.getName(), branch.getCommitSha1());

            m_currentRepository.InsertBranch(newRBranch);

            // update RTB in LR

            m_currentRepository.GetBranches().remove(m_currentRepository.GetHeadBranch().getName());

            newRTBranch = new RTBranch(Repository.m_pathToMagitDirectory.resolve("branches").
                    resolve(branch.getName()), branch.getCommitSha1());

            m_currentRepository.InsertBranch(newRTBranch);

            m_currentRepository.SetHeadBranch(newRTBranch);

            checkOut(m_currentRepository.GetHeadBranch().getName());

            //m_currentRepository.GetCommitsMap();

            initNewPaths(m_currentRepository.GetRepositoryPath(), m_currentRepository);
        }

    }

    public void Push() throws IOException {

        m_currentRepository.GetCommitsMap();

        Path currMagitPath = Repository.m_pathToMagitDirectory;
        Path currRepoPath = Repository.m_repositoryPath;
        boolean flag = false;

        Repository RR = new Repository(((LocalRepository)m_currentRepository).getRemoteRepoName(),
                ((LocalRepository)m_currentRepository).getRemoteRepoLocation(), true);

        Path RRpath = RR.GetRepositoryPath();
        Path RRMagit = Repository.m_pathToMagitDirectory;

        RR.GetCommitsMap();

        Repository.m_pathToMagitDirectory = currMagitPath;
        Repository.m_repositoryPath = currRepoPath;


        Commit branchCommit1;
        Commit branchCommit2 = null;

        Branch branch;
        RTBranch newRTBranch;
        Branch newBranch;

        initNewPaths(RRpath, m_currentRepository);

        branchCommit1 = new Commit(m_currentRepository.GetHeadBranch().getCommitSha1());
        String headCommit = branchCommit1.getSha1();


        //Repository.m_pathToMagitDirectory = currMagitPath;
        //Repository.m_repositoryPath = currRepoPath;

        //initNewPaths(m_currentRepository.GetRepositoryPath(), RR);

        //update commits of the branch

        Repository.m_repositoryPath = RRpath;
        Repository.m_pathToMagitDirectory = RRMagit;

        while(((branchCommit1.getFirstPrecedingSha1() != null && !branchCommit1.getFirstPrecedingSha1().isEmpty()) ||
                (branchCommit1.getSecondPrecedingSha1() != null && !branchCommit1.getSecondPrecedingSha1().isEmpty()))
                ||(branchCommit2 != null && (
                (branchCommit2.getFirstPrecedingSha1() != null && !branchCommit2.getFirstPrecedingSha1().isEmpty())||
                        branchCommit2.getSecondPrecedingSha1() != null && !branchCommit2.getSecondPrecedingSha1().isEmpty()))){

            if(branchCommit1.getFirstPrecedingSha1() != null && !branchCommit1.getFirstPrecedingSha1().isEmpty() ||
                    (branchCommit1.getSecondPrecedingSha1() != null && !branchCommit1.getSecondPrecedingSha1().isEmpty())) {
                branchCommit1.getRootFolder().saveInObjects();
                Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(branchCommit1.getSha1())
                        , branchCommit1.toString());
                branchCommit1 = RR.GetCommitsMapObj().get(branchCommit1.getFirstPrecedingSha1());
                branchCommit2 = RR.GetCommitsMapObj().get(branchCommit1.getSecondPrecedingSha1());

            }else if( branchCommit2 != null &&
                    (branchCommit2.getFirstPrecedingSha1() != null && !branchCommit2.getFirstPrecedingSha1().isEmpty())||
                    branchCommit2.getSecondPrecedingSha1() != null && !branchCommit2.getSecondPrecedingSha1().isEmpty()){
                branchCommit2.getRootFolder().saveInObjects();
                Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(branchCommit2.getSha1())
                        , branchCommit2.toString());
                branchCommit1 = RR.GetCommitsMapObj().get(branchCommit2.getFirstPrecedingSha1());
                branchCommit2 = RR.GetCommitsMapObj().get(branchCommit2.getSecondPrecedingSha1());
            }
        }

        //if(m_currentRepository.GetHeadBranch() instanceof RTBranch){
            //VALIDATION CHECK

            branch = RR.GetBranches().get(m_currentRepository.GetHeadBranch().getName());
            if(RR.GetHeadBranch().getName().equals(m_currentRepository.GetHeadBranch().getName())) {
                flag = true;
            }
            RR.GetBranches().remove(branch.getName());

            // update RB in RR


            RR.GetBranches().remove(branch.getName());

            newBranch = new Branch(Repository.m_pathToMagitDirectory.resolve("branches").
                        resolve(branch.getName()), headCommit);

            RR.InsertBranch(newBranch);
            if(flag) {
                    RR.SetHeadBranch(newBranch);
            }

            //checkOut(m_currentRepository.GetHeadBranch().getName());

            //RR.GetCommitsMap();

            //initNewPaths(m_currentRepository.GetRepositoryPath(), m_currentRepository);
        //}

    }

}
