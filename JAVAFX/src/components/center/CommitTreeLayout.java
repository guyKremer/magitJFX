package components.center;

import Engine.MagitObjects.Commit;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.layout.Layout;
import puk.team.course.magit.ancestor.finder.*;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

// simple test for scattering commits in imaginary tree, where every 3rd node is in a new 'branch' (moved to the right)
public class CommitTreeLayout implements Layout {
    private final Map<Commit, ICell> mf_MapCommitToIcell;
    private double m_LayoutX = 10;
    private double m_LayoutY = 50;

    public CommitTreeLayout(Map<Commit, ICell> i_MapCommitToIcell) {
        mf_MapCommitToIcell = i_MapCommitToIcell;
    }

    @Override
    public void execute(Graph graph) {
        List<Commit> commitListByOrder = sortListByOrderOfCreation();
        Map<String, Commit> mapPrevSHA1ToCommitFather = initMap();
        List<Commit> openCommits = new ArrayList<Commit>();


        for (Commit currentCommit : commitListByOrder) {
            Commit commitSon;

            commitSon = findSon(currentCommit, openCommits);
            if (commitSon != null) {
                ICell sonICell = mf_MapCommitToIcell.get(commitSon);
                double layoutSonX = graph.getGraphic(sonICell).getLayoutX();

                relocateCommit(graph, currentCommit, layoutSonX);
                openCommits.add(currentCommit);
                openCommits.remove(commitSon);

                //relocate below
            } else {
                m_LayoutX += 50;
                relocateCommit(graph, currentCommit, m_LayoutX);
                openCommits.add(currentCommit);
            }

               /* openCommits.remove(commitFather);
                openCommits.add(currentCommit);*/
        } /*else
        {
            relocateOpenCommit(currentCommit, graph);
        }*/
    }


    private Commit findSon(Commit i_CurrentCommit, List<Commit> i_OpenCommits) {
        return i_OpenCommits
                .stream()
                .filter(commit ->
                {
                    boolean firstPrev = false, secondPrev = false;

                    if (!commit.getFirstPrecedingSha1().isEmpty()) {
                        firstPrev = commit.getFirstPrecedingSha1().equals(i_CurrentCommit.getSha1());
                    }

                    if (!commit.getSecondPrecedingSha1().isEmpty()) {
                        secondPrev = commit.getSecondPrecedingSha1().equals(i_CurrentCommit.getSha1());
                    }
                    return secondPrev || firstPrev;
                })
                .findAny()
                .orElse(null);
    }

    private void relocateCommit(Graph i_Graph, Commit i_CurrentCommit, double i_LayoutXToRelocate) {
        ICell fatherICell = mf_MapCommitToIcell.get(i_CurrentCommit);

        m_LayoutY += 50;

        i_Graph.getGraphic(fatherICell).relocate(i_LayoutXToRelocate, m_LayoutY);

    }
//        increaseLayouts();

    private List<Commit> sortListByOrderOfCreation() {
        List<Commit> temp = mf_MapCommitToIcell
                .keySet()
                .stream()
                .sorted(Comparator.comparing(Commit::getDateOfCreation))
                .collect(Collectors.toList());

        Collections.reverse(temp);

        return temp;
    }

    private Map<String, Commit> initMap() {
        Map<String, Commit> mapPrevSHA1ToCommitFather = new HashMap<>();

        mf_MapCommitToIcell
                .keySet()
                .stream()
                .forEach(commit ->
                {
                    try {
                        addPrevSha1AndCommit(mapPrevSHA1ToCommitFather, commit, new Commit(commit.getFirstPrecedingSha1()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        addPrevSha1AndCommit(mapPrevSHA1ToCommitFather, commit, new Commit(commit.getSecondPrecedingSha1()));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return mapPrevSHA1ToCommitFather;
    }

    private void addPrevSha1AndCommit(Map<String, Commit> mapPrevSHA1ToCommitFather, Commit commit, Commit prevCommit) {
        if (thereIsPrevCommit(prevCommit)) {
            mapPrevSHA1ToCommitFather.put(prevCommit.getSha1(), commit);
        }
    }

    private boolean thereIsPrevCommit(Commit i_GetPrevCommit) {
        return i_GetPrevCommit != null;
    }
}
