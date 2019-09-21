package Engine;

import Engine.MagitObjects.Branch;
import Engine.MagitObjects.Commit;
import Engine.MagitObjects.Repository;

import org.apache.commons.io.FileUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;
import puk.team.course.magit.ancestor.finder.CommitRepresentative;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    public void initializeRepository(String i_pathToRepo,String i_repoName)throws FileAlreadyExistsException,java.io.IOException{
        Path path = Paths.get(i_pathToRepo);

        if(!Files.exists(path)){
            Files.createDirectories(path);
        }
        if(!Files.exists(path.resolve(".magit"))){
            m_currentRepository=new Repository(i_repoName,i_pathToRepo,false);
        }
        else{
            throw new FileAlreadyExistsException(i_pathToRepo);
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
            m_currentRepository = new Repository(lines.get(0),i_pathToRepo, true);
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

    public void resetBranchSha1(String i_branchName, String i_sha1)throws FileNotFoundException,IOException {
        isRepositoryInitialized();
        m_currentRepository.resetBranchSha1(i_branchName,i_sha1);
    }

    public void setCurrentRepository(Repository repo){
        this.m_currentRepository=repo;
    }


    public List<Commit> GetHeadBranchCommitHistory()throws FileNotFoundException,IOException{
        isRepositoryInitialized();
        return m_currentRepository.GetHeadBranchCommitHistory();
    }

    public Branch GetHeadBranch() {
        System.out.println(m_currentRepository);
        return m_currentRepository.GetHeadBranch();
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
        //stam
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
        m_currentRepository.AddBranch(i_branchName,i_checkout);
    }

    public void Merge(String i_theirs)throws FileNotFoundException,IOException{
        Branch theirsBranch =  m_currentRepository.GetBranch(i_theirs);
        Commit nca;
        //if exists
        if(theirsBranch!= null){
            nca =  getAncestor(theirsBranch);
            System.out.println(nca);
            checkConflicts(nca);
        }
    }

    private void checkConflicts(Commit nca) {
    }

    private Commit getAncestor(Branch i_theirsBranch)throws FileNotFoundException,IOException {
        String oursSha1 = m_currentRepository.GeCurrentCommit().getSha1();
        String theirsSha1 = i_theirsBranch.getCommitSha1();
        AncestorFinder anf = new AncestorFinder(sha1->{
           try{
              return new Commit((sha1));
           }
           catch (IOException e ){
                return null;
           }
        });
        return new Commit(anf.traceAncestor(oursSha1,theirsSha1));
    }
}
