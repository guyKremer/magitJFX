package Engine.MagitObjects;

import Engine.Engine;
import Engine.MagitObjects.FolderItems.Blob;
import Engine.MagitObjects.FolderItems.Folder;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import puk.team.course.magit.ancestor.finder.*;
import Engine.Conflict;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Commit implements CommitRepresentative {
    private String m_sha1;
    private String m_message;
    private Folder m_rootFolder;
    private List<String> m_prevCommitSha1Array  = new ArrayList<String>();
    private String m_dateOfCreation;
    private String m_creator;

    public Commit(){
        m_message=null;
        m_rootFolder=null;
        m_prevCommitSha1Array=null;
        m_dateOfCreation=null;
        m_creator=null;
    }

    public Date GetDate(){
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy-hh:mm:ss:SSS");
        Date date = null ;
        try {
            date = format.parse(m_dateOfCreation);
        }
        catch (ParseException e){
            e.printStackTrace();
        }

        return date;
    }

    public Commit(String i_sha1) throws FileNotFoundException,IOException {
        unzipCommit(i_sha1);
    }

    @Override
    public boolean equals(Object o) {

        if (o == this) {
            return true;
        }

        Commit c = (Commit) o;

        // Compare the data members and return accordingly
        return (this.m_sha1).equals(c.getSha1());
    }

    @Override
    public int hashCode(){
        return 1;
    }

    public void setSha1(String m_sha1) {
        this.m_sha1 = m_sha1;
    }

    public Commit(String i_message, Folder i_root, String i_FirstCommitSha1, String i_SecondCommitSha1,
                  String i_dateOfCreation, String i_creator)
    {
        m_message=i_message;
        m_rootFolder=i_root;
        m_rootFolder.saveInObjects();
        m_prevCommitSha1Array=new ArrayList<String>();
        setFirstPrecedingSha1(i_FirstCommitSha1);
        setSecondPrecedingSha1(i_SecondCommitSha1);
        m_dateOfCreation=i_dateOfCreation;
        m_creator=i_creator;
        m_sha1=createSha1();
    }

    public String getSha1(){
        return m_sha1;
    }

    @Override
    public String getFirstPrecedingSha1() {
        return m_prevCommitSha1Array.get(0)==null? "":m_prevCommitSha1Array.get(0);
    }

    @Override
    public String getSecondPrecedingSha1() {
        return m_prevCommitSha1Array.get(1)==null? "":m_prevCommitSha1Array.get(1);
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
        if(i_sha1 == null||i_sha1.isEmpty()||i_sha1.equals("null")){
            if(m_prevCommitSha1Array.isEmpty()){
                m_prevCommitSha1Array.add(0, null);
            }
            else{
                m_prevCommitSha1Array.set(0,null);
            }
        }
        else{
            if(m_prevCommitSha1Array.isEmpty()){
                m_prevCommitSha1Array.add(0, i_sha1);
            }
            else{
                m_prevCommitSha1Array.set(0,i_sha1);
            }
        }
    }

    public void setSecondPrecedingSha1(String i_sha1){
        if(i_sha1 == null ||i_sha1.isEmpty()|| i_sha1.equals("null")){
            if(m_prevCommitSha1Array.size()<2){
                m_prevCommitSha1Array.add(1, null);
            }
            else{
                m_prevCommitSha1Array.set(1,null);
            }
        }
        else{
            if(m_prevCommitSha1Array.size()<2){
                m_prevCommitSha1Array.add(1, i_sha1);
            }
            else{
                m_prevCommitSha1Array.set(1,i_sha1);
            }
        }
    }

    public void setM_dateOfCreation(String i_date){
        m_dateOfCreation = i_date;
    }

    public void setM_creator(String i_creator){
        m_creator=i_creator;
    }

    public void unzipRootDirectory(List<String> i_commitLines){
        m_rootFolder = new Folder(Repository.m_repositoryPath,i_commitLines.get(0),i_commitLines.get(5),i_commitLines.get(4));
        m_rootFolder.unzipAndSaveFolder(i_commitLines.get(0));
    }

    public void flush(){
        m_rootFolder.flushToWc();
    }
    public void flushForMerge(Commit ancestor, Commit ours){
        m_rootFolder.flushForMergeToWc(ancestor,ours);
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
        string.append(m_prevCommitSha1Array.get(0));
        string.append(System.getProperty("line.separator"));
        string.append(m_prevCommitSha1Array.get(1));
        string.append(System.getProperty("line.separator"));
        string.append(m_message);
        string.append(System.getProperty("line.separator"));
        string.append(m_dateOfCreation);
        string.append(System.getProperty("line.separator"));
        string.append(m_creator);

        return string.toString();
    }

    public Map <Path,Conflict> findConflicts(Set<Integer> conflictsSet, Commit ncaCommit, Commit theirsCommit) {
        Map<Path, String> ncaPathToSha1Map = new HashMap<>();
        Map<Path, String> theirsPathToSha1Map = new HashMap<>();
        Map<Path, String> oursPathToSha1Map = new HashMap<>();
        Map <Path,Conflict> res = new HashMap<>();
        Integer conflictRep  = 0b00000000;

        ncaCommit.getRootFolder().createPathToSha1Map(ncaPathToSha1Map);
        theirsCommit.getRootFolder().createPathToSha1Map(theirsPathToSha1Map);
        getRootFolder().createPathToSha1Map(oursPathToSha1Map);


        for (Map.Entry<Path,String> entry : ncaPathToSha1Map.entrySet()){
            if(entry.getKey().equals(m_rootFolder.getPath())||ncaCommit.getRootFolder().GetItem(entry.getValue()).GetType().equals("folder")){
                continue;
            }
            conflictRep = calculateConflictRep(entry.getKey(),ncaPathToSha1Map,oursPathToSha1Map,theirsPathToSha1Map);
            if(conflictsSet.contains(conflictRep)){
                if(conflictRep.equals(new Integer(0b110011))){
                    continue;
                }

                Blob blob = (Blob)ncaCommit.getRootFolder().GetItem(entry.getValue());
                Conflict conflictToInsert = new Conflict(entry.getKey(),blob.GetContent(),null,null);
                res.put(entry.getKey(),conflictToInsert);
            }
        }
        for (Map.Entry<Path,String> entry : oursPathToSha1Map.entrySet()){
            if(entry.getKey().equals(m_rootFolder.getPath())||m_rootFolder.GetItem(entry.getValue()).GetType().equals("folder")){
                continue;
            }
            conflictRep = calculateConflictRep(entry.getKey(),ncaPathToSha1Map,oursPathToSha1Map,theirsPathToSha1Map);

            if(conflictsSet.contains(conflictRep)){
                if(conflictRep.equals(new Integer(0b110011))){
                    FileUtils.deleteQuietly(entry.getKey().toFile());
                    continue;
                }
                Blob blob = (Blob) m_rootFolder.GetItem(entry.getValue());
                if(res.containsKey(entry.getKey())){
                    res.get(entry.getKey()).setOursContent(blob.GetContent());
                }
                else{
                    Conflict conflictToInsert = new Conflict(entry.getKey(),null,blob.GetContent(),null);
                    res.put(entry.getKey(),conflictToInsert);
                }
            }
        }
        for (Map.Entry<Path,String> entry : theirsPathToSha1Map.entrySet()){
            if(entry.getKey().equals(m_rootFolder.getPath()) || theirsCommit.getRootFolder().GetItem(entry.getValue()).GetType().equals("folder")){
                continue;
            }
            conflictRep = calculateConflictRep(entry.getKey(),ncaPathToSha1Map,oursPathToSha1Map,theirsPathToSha1Map);
            if(conflictsSet.contains(conflictRep)){
                if(conflictRep.equals(new Integer(0b110011))){
                    continue;
                }

                Blob blob = (Blob)theirsCommit.getRootFolder().GetItem(entry.getValue());
                if(res.containsKey(entry.getKey())){
                    res.get(entry.getKey()).setTheirsContent(blob.GetContent());
                }
                else{
                    Conflict conflictToInsert = new Conflict(entry.getKey(),null,null,blob.GetContent());
                    res.put(entry.getKey(),conflictToInsert);
                }
            }
        }
        return res;
    }

    private Integer calculateConflictRep(Path pathToFind,Map<Path, String> ncaPathToSha1Map,Map<Path, String> oursPathToSha1Map, Map<Path, String> theirsPathToSha1Map) {
        Integer conflictRep = 0b00000000;
        String ncaCurrentFileSha1 = ncaPathToSha1Map.get(pathToFind);
        String oursCurrentFileSha1 = oursPathToSha1Map.get(pathToFind);
        String theirsCurrentFileSha1 = theirsPathToSha1Map.get(pathToFind);


        // if exists in nca
        if (ncaPathToSha1Map.containsKey(pathToFind)){
            conflictRep=turnOnBit(0,conflictRep);
        }
        // if exists in ours
        if (oursPathToSha1Map.containsKey(pathToFind)){
            conflictRep=turnOnBit(1,conflictRep);
        }
        //if exists in theirs
        if (theirsPathToSha1Map.containsKey(pathToFind)){

            conflictRep=turnOnBit(2,conflictRep);
        }
        //nca vs ours
        if( oursCurrentFileSha1 !=null && !oursCurrentFileSha1.equals(ncaCurrentFileSha1)
                ||  ncaCurrentFileSha1 !=null && !ncaCurrentFileSha1.equals(oursCurrentFileSha1)){

            conflictRep=turnOnBit(3,conflictRep);
        }
        //nca vs theirs
        if( theirsCurrentFileSha1 !=null && !theirsCurrentFileSha1.equals(ncaCurrentFileSha1)
            ||ncaCurrentFileSha1 !=null && !ncaCurrentFileSha1.equals(theirsCurrentFileSha1)){


            conflictRep=turnOnBit(4,conflictRep);
        }
        //ours vs theirs
        if(theirsCurrentFileSha1 !=null && !theirsCurrentFileSha1.equals(oursCurrentFileSha1)
            || oursCurrentFileSha1 !=null && !oursCurrentFileSha1.equals(theirsCurrentFileSha1) ){
            conflictRep=turnOnBit(5,conflictRep);
        }

        return conflictRep;
    }

    private Integer turnOnBit(int index,Integer conflictByteRep) {
        Integer mask = 0b100000;
        mask = mask>>index;
        return conflictByteRep | mask;
    }
}
