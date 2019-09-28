package Engine.MagitObjects.FolderItems;

import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

import Engine.MagitObjects.Commit;
import Engine.MagitObjects.Repository;
import Engine.Status;

public abstract class FolderItem implements Comparable<FolderItem>{

    protected String m_sha1=null;
    protected String m_name;
    protected Path m_path;
    protected String m_type=null;
    protected String m_updater=null;
    protected String m_lastModified=null;

    public FolderItem(Path i_path){
        m_path=i_path;
        m_name= m_path.getFileName().toString();
    }

    public Path getPath() {
        return m_path;
    }

    public void setPath(Path i_path) {
        this.m_path = i_path;
    }

    public void SetUpdater(String m_updater) {
        this.m_updater = m_updater;
    }

    public void SetLastModified(String m_lastModified) {
        this.m_lastModified = m_lastModified;
    }

    public String GetUpdater() {
        return m_updater;
    }

    public String GetLastModified() {
        return m_lastModified;
    }

    public String GetType() {
        return m_type;
    }

    public String GetName() {
        return m_name;
    }

    public String getSha1(){
        return m_sha1;
    }

    public FolderItem(){}

    public abstract void createSha1();

    public abstract void saveInObjects();

    public abstract void flushToWc();

    @Override
    public String toString() {
        return String.format(m_name+","+ m_sha1 + "," + m_type + "," +m_updater +"," + m_lastModified );
    }

    @Override
    public int compareTo(FolderItem item) {
        if (m_sha1 == null || item.getSha1() == null){
            return 0;
        }

        return m_sha1.compareTo(item.getSha1());
    }

    public abstract void fullToString(List<String> str);

    protected abstract void UpdateChangedFolderItemsRec(Status i_status,Map<Path, FolderItem> i_map);

    public abstract void flushForMergeToWc(Commit ancestor, Commit ours);

    protected void updateAuthorAndLastModifiedIfNeeded(Status i_status,Map<Path, FolderItem> i_map){
        if(changed(i_status)) {
            m_lastModified = Repository.m_simpleDateFormat.format(new Date());
            /*
            String pattern = "dd.MM.yyyy-hh:mm:ss:SSS";
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
            m_lastModified = simpleDateFormat.format(new Date());
            */
            m_updater=i_status.getUserName();
        }
        else{
            //System.out.println(m_path);
            m_lastModified = i_map.get(m_path).GetLastModified();
            m_updater = i_map.get(m_path).GetUpdater();
        }
    }

    private boolean changed(Status i_status) {
        Map <String,String> changedFilesMap = i_status.getChangedFilesMap();
        return changedFilesMap.containsKey(m_path.toString());
    }
}
