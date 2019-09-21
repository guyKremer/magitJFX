package Engine.MagitObjects;

import Engine.Engine;
import Engine.MagitObjects.FolderItems.Folder;
import Engine.MagitObjects.FolderItems.FolderItem;
import com.sun.xml.internal.ws.server.ServerRtException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import Engine.Status;
import puk.team.course.magit.ancestor.finder.*;


import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit implements CommitRepresentative {
    private String m_sha1;
    private String m_message;
    private Folder m_rootFolder;
    private List<String> m_prevCommitSha1Array ;
    private String m_dateOfCreation;
    private String m_creator;

    public Commit(){
        m_message=null;
        m_rootFolder=null;
        m_prevCommitSha1Array=null;
        m_dateOfCreation=null;
        m_creator=null;
    }

    public Commit(String i_sha1) throws FileNotFoundException,IOException {
        unzipCommit(i_sha1);
    }

    public Commit(String i_message, Folder i_root, String i_FirstCommitSha1,String i_SecondCommitSha1,
                  String i_dateOfCreation, String i_creator)
    {
        m_message=i_message;
        m_rootFolder=i_root;
        m_rootFolder.saveInObjects();
        m_prevCommitSha1Array=new ArrayList<String>();
        m_prevCommitSha1Array.add(i_FirstCommitSha1);
        m_prevCommitSha1Array.add(i_SecondCommitSha1);
        m_dateOfCreation=i_dateOfCreation;
        m_creator=i_creator;
        m_sha1=createSha1();
    }

    public String getSha1(){
        return m_sha1;
    }

    @Override
    public String getFirstPrecedingSha1() {
        if(m_prevCommitSha1Array!= null){
            return m_prevCommitSha1Array.get(0);
        }
        else{
            return "";
        }
    }

    @Override
    public String getSecondPrecedingSha1() {
        if(m_prevCommitSha1Array!= null){
                return m_prevCommitSha1Array.get(1);
        }
        else{
            return null;
        }
    }

    public String getMessage(){return m_message;}
    public List<String> getPrevCommitSha1Array(){
        return m_prevCommitSha1Array;
    }
    public String getDateOfCreation(){return m_dateOfCreation;}
    public String getCreator(){return m_creator;}

    public void setM_message(String i_message){
        m_message=i_message;
    }
    public void setFirstPrecedingSha1(String i_sha1){
        if(i_sha1.equals("null")){
            m_prevCommitSha1Array=null;
        }
        else{
            m_prevCommitSha1Array.add(0, i_sha1);
        }
    }

    public void setSecondPrecedingSha1(String i_sha1){
        if(i_sha1.equals("null")){
            m_prevCommitSha1Array=null;
        }
        else{
            m_prevCommitSha1Array.add(1, i_sha1);
        }
    }

    public void setM_dateOfCreation(String i_date){
        m_dateOfCreation = i_date;
    }

    public void setM_creator(String i_creator){
        m_creator=i_creator;
    }

    public void unzipRootDirectory(List<String> i_commitLines){
        m_rootFolder = new Folder(Repository.m_repositoryPath,i_commitLines.get(0),i_commitLines.get(4),i_commitLines.get(3));
        m_rootFolder.unzipAndSaveFolder(i_commitLines.get(0));
    }

    public void flush(){
        m_rootFolder.flushToWc();
    }
    public void Commit(Folder i_WC,String i_message,String i_dateOfCreation,String i_user,String i_firstPrevCommitSha1 ,String i_secondPrevCommitSha1){
        // Check If Sha1 Exists
        m_rootFolder = i_WC;
       // m_rootFolder.saveInObjects();
        m_creator=i_user;
        m_message= i_message;
        m_dateOfCreation=i_dateOfCreation;
        m_prevCommitSha1Array.add(0,i_firstPrevCommitSha1);
        m_prevCommitSha1Array.add(1,i_secondPrevCommitSha1);
        m_sha1=createSha1();

        try{
            Engine.Utils.zipToFile(Repository.m_pathToMagitDirectory.resolve("objects").resolve(m_sha1)
                                    ,this.toString());
        }
        catch (java.io.IOException e){

        }
    }

    public String createSha1() {
        StringBuilder commitContent = new StringBuilder();

        commitContent.append(m_rootFolder.getSha1() + getFirstPrecedingSha1() + getSecondPrecedingSha1()
                             + m_dateOfCreation + m_creator);
        m_sha1 = DigestUtils.sha1Hex(commitContent.toString());
        return m_sha1;

    }

    public Folder getRootFolder(){
        return m_rootFolder;
    }

    public void unzipCommit(String i_commitSha1) throws FileNotFoundException,IOException {
        if( ! Files.exists(Repository.m_pathToMagitDirectory.resolve("objects").resolve(i_commitSha1))){
            throw new FileNotFoundException(Repository.m_pathToMagitDirectory.resolve("objects").resolve(i_commitSha1).toString()
                    + "doesn't exist");
        }
        File unZippedCommit=Engine.Utils.UnzipFile(i_commitSha1);
        List<String> lines = Files.readAllLines(unZippedCommit.toPath());
        unZippedCommit.delete();
        unzipRootDirectory(lines);
        readPartialCommitLines(lines);
        m_sha1=i_commitSha1;
    }

    public void readPartialCommitLines(List<String> i_lines){

        for (int i=0; i < i_lines.size(); i++) {
            switch(i){

                case 1:setFirstPrecedingSha1(i_lines.get(i));
                    break;
                case 2:setSecondPrecedingSha1(i_lines.get(i));
                    break;

                case 3:setM_message(i_lines.get(i));
                    break;

                case 4: setM_dateOfCreation(i_lines.get(i));
                    break;

                case 5:setM_creator(i_lines.get(i));
            }
        }

    }
    @Override
    public String toString() {
        StringBuilder string = new StringBuilder();

        string.append(m_rootFolder.getSha1());
        string.append(System.getProperty("line.separator"));
        string.append(getFirstPrecedingSha1());
        string.append(System.getProperty("line.separator"));
        string.append(getSecondPrecedingSha1());
        string.append(System.getProperty("line.separator"));
        string.append(m_message);
        string.append(System.getProperty("line.separator"));
        string.append(m_dateOfCreation);
        string.append(System.getProperty("line.separator"));
        string.append(m_creator);

        return string.toString();
    }

}
