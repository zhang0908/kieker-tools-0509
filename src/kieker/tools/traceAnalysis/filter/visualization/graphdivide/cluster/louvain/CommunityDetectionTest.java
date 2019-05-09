package kieker.tools.traceAnalysis.filter.visualization.graphdivide.cluster.louvain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommunityDetectionTest {

    public void communityDetection(double[][] G){
        Louvain louvain = new Louvain();
        louvain.init(G);
        louvain.louvain();

        for(int i=0;i<louvain.global_n;i++){
            System.out.print(Integer.toString(louvain.global_cluster[i]) + " ");
        }
        System.out.println();
        ArrayList list[] = new ArrayList[louvain.global_n];
        for(int i=0;i<louvain.global_n;i++){
            list[i]=new ArrayList<Integer>();
        }
        for(int i=0;i<louvain.global_n;i++){
            list[louvain.global_cluster[i]].add(i);
        }
        for(int i=0;i<louvain.global_n;i++){
            if(list[i].size()==0) continue;
            for(int j=0;j<list[i].size();j++){
                System.out.print(list[i].get(j).toString()+" ");
            }
            System.out.println();
        }

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
        CommunityDetectionTest cd = new CommunityDetectionTest();
        cd.communityDetection(G);
    }
}
