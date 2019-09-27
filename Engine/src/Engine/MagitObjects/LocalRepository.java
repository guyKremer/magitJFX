package Engine.MagitObjects;

import java.io.IOException;

public class LocalRepository extends Repository {
    public LocalRepository(String i_name, String i_repository, boolean i_exists) throws IOException {
        super(i_name, i_repository, i_exists);
    }

    private String remoteRepoName;
    private String remoteRepoLocation;

    public void setRemoteRepoLocation(String remoteRepoLocation) {
        this.remoteRepoLocation = remoteRepoLocation;
    }

    public String getRemoteRepoLocation() {
        return remoteRepoLocation;
    }

    public void setRemoteRepoName(String remoteRepoName) {
        this.remoteRepoName = remoteRepoName;
    }

    public String getRemoteRepoName() {
        return remoteRepoName;
    }
}
