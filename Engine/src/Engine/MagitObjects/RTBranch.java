package Engine.MagitObjects;

import org.apache.commons.io.FileUtils;

import java.nio.charset.Charset;
import java.nio.file.Path;

public class RTBranch extends Branch {
    public RTBranch(Path i_pathToBranch, String i_commitSha1)throws java.io.IOException {
        super(i_pathToBranch,i_commitSha1);
    }

    @Override
    public void flushBranch()throws java.io.IOException{
        FileUtils.writeStringToFile(m_pathToBranch.toFile(),
                m_commitSha1 + System.lineSeparator() + "true",
                Charset.forName("utf-8"),false);
    }
}
