package Engine.MagitObjects;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.charset.Charset;

public class LocalRepository extends Repository {
    public LocalRepository(String i_name,
                           String i_repository,
                           boolean i_exists,
                           String i_remoteRepoLocation,
                           String i_remoteRepoName) throws IOException {
        super(i_name, i_repository, i_exists);

        remoteRepoLocation = i_remoteRepoLocation;
        remoteRepoName = i_remoteRepoName;

        FileUtils.writeStringToFile(m_pathToMagitDirectory.resolve("RepoName").toFile(),
                m_name + System.lineSeparator() + i_remoteRepoLocation + System.lineSeparator() + i_remoteRepoName,
                Charset.forName("utf-8"),false);
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
