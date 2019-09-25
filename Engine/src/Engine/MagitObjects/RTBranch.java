package Engine.MagitObjects;

import java.nio.file.Path;

public class RTBranch extends Branch {
    public RTBranch(Path i_pathToBranch, String i_commitSha1)throws java.io.IOException {
        super(i_pathToBranch,i_commitSha1);
    }
}
