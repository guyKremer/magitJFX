package Engine.MagitObjects;

import Engine.Engine;
import org.apache.commons.io.FileUtils;
import puk.team.course.magit.ancestor.finder.AncestorFinder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Branch {
    protected Path m_pathToBranch;
    protected String m_commitSha1;
    protected String m_name;

    public Branch(){};

    public Branch(Path i_pathToBranch, String i_commitSha1)throws java.io.IOException {
        m_pathToBranch = i_pathToBranch;
        m_name = i_pathToBranch.getFileName().toString();
        m_commitSha1 = i_commitSha1;
        if(m_commitSha1.equals("null")){
            m_commitSha1=null;
        }
        flushBranch();
    }

    public Branch(Path i_pathToBranch, String i_name, String i_commitSha1) throws IOException {
        m_pathToBranch = i_pathToBranch;
        m_name = i_name;
        m_commitSha1 = i_commitSha1;
        if(m_commitSha1.equals("null")){
            m_commitSha1=null;
        }
        flushBranch();
    }

    public Path getPathToBranch(){
        return m_pathToBranch;
    }
    public String getName(){
        return m_name;
    }

    public Branch(Path i_headBranch)throws java.io.IOException {
        String headBranch=FileUtils.readFileToString(i_headBranch.toFile(),Charset.forName("utf-8"));
        m_pathToBranch=Repository.m_pathToMagitDirectory.resolve("branches").resolve(headBranch);
        m_commitSha1=FileUtils.readFileToString(m_pathToBranch.toFile(), Charset.forName("utf-8"));
        if(m_commitSha1.equals("null")){
            m_commitSha1=null;
        }
    }

    public String getCommitSha1(){
        return m_commitSha1;
    }

    public void setCommitSha1(String i_newSha1){
        m_commitSha1=i_newSha1;
    }

    public void flushBranch()throws java.io.IOException{
        //System.out.println("inn");
        FileUtils.writeStringToFile(m_pathToBranch.toFile(), m_commitSha1, Charset.forName("utf-8"),false);
    }

    public String getCommitMsg() throws FileNotFoundException,IOException {
        if(m_commitSha1 == null || m_commitSha1.isEmpty()){
            throw new FileNotFoundException ("Nothing was committed in  " + m_name);
        }
        File unZippedCommit= Engine.Utils.UnzipFile(m_commitSha1);
        List<String> lines = Files.readAllLines(unZippedCommit.toPath());
        unZippedCommit.delete();
        return lines.get(2);
    }


    public void SetName(String i_name) {
        m_name = i_name;
    }

    public void SetPathToBranch(Path i_pathToBranch){
        i_pathToBranch = m_pathToBranch;
    }
}
