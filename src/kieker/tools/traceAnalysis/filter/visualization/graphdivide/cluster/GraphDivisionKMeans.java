package kieker.tools.traceAnalysis.filter.visualization.graphdivide.cluster;

import smile.clustering.KMeans;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphDivisionKMeans {

    //k:要分成多少个部分
    public static void kMeans(double[][] G, int k, Map<Integer, List<Integer>> nodeClusterMap){
        KMeans km = new KMeans(G, k);
        
        for(int i = 0; i < km.getNumClusters(); i ++){
        	nodeClusterMap.put(i, new ArrayList<Integer>());
        }
        int[] lables = km.getClusterLabel();
        for(int i = 0; i < lables.length; i++){
        	nodeClusterMap.get(lables[i]).add(i);
        }
        System.out.println(nodeClusterMap);
    }

    public static void main(String[] args) {
        double G[][] = new double[8][8];
        G[0][1] = 3;
        G[0][2] = 3;
        G[0][3] = 3;
        G[1][2] = 3;
        G[1][3] = 3;
        G[1][7] = 1;
        G[2][3] = 3;
        G[4][5] = 3;
        G[4][6] = 2;
        G[4][7] = 2;
        G[5][6] = 2;
        G[5][7] = 2;

        G[1][0] = 3;
        G[2][0] = 3;
        G[3][0] = 3;
        G[2][1] = 3;
        G[3][1] = 3;
        G[7][1] = 1;
        G[3][2] = 3;
        G[5][4] = 3;
        G[6][4] = 2;
        G[7][4] = 2;
        G[6][5] = 2;
        G[7][5] = 2;
        Map<Integer, List<Integer>> nodeClusterMap = new HashMap<Integer, List<Integer>>();
        GraphDivisionKMeans sw = new GraphDivisionKMeans();
        sw.kMeans(G, 2, nodeClusterMap);
    }
}
