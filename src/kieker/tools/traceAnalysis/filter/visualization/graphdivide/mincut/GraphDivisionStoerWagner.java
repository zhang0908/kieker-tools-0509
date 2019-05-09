package kieker.tools.traceAnalysis.filter.visualization.graphdivide.mincut;


import java.util.ArrayList;
import java.util.List;

/**
 * 1.min=MAXINT，固定一个顶点P
 *
 * 2.从点P用“类似”prim的s算法扩展出“最大生成树”，记录最后扩展的顶点和最后扩展的边
 *
 * 3.计算最后扩展到的顶点的切割值（即与此顶点相连的所有边权和），若比min小更新min
 *
 * 4.合并最后扩展的那条边的两个端点为一个顶点（当然他们的边也要合并，这个好理解吧？）
 *
 * 5.转到2，合并N-1次后结束
 *
 * 6.min即为所求，输出min
 *
 * 　prim本身复杂度是O(n^2)，合并n-1次，算法复杂度即为O(n^3)，如果在prim中加堆优化，复杂度会降为O((n^2)logn)
 *
 * 　帮助理解（reference http://www.cnblogs.com/ihopenot/p/5986772.html）：
 * 考虑任意两个点为s，t，如果全局最小割中s，t不在一个集合中，那么显然全局最小割即为s-t最小割。
 * 否则我们将s，t缩成一个节点对于答案是没有影响的。基于这一点，每次将问题规模减小后求解。
 * 一开始选择的节点是作为s-t的中间节点集，因为每次扩展是选取联系度最大的点扩展，
 * 所以中间节点集中点互相间的联系度是大于st到中间点集的联系度的，而最后加入的点t的联系度是最小的，
 * 所以最小割即为这个点的联系度，即为s通过中间节点集到t的流量加上s直接到t的流量。所以就证明了每次拓展求出的是s-t的最小割。
 */
public class GraphDivisionStoerWagner {
	
	public static int stoerWagner2(int[][] G, boolean[] vaildVs, List<Integer> partAList, List<Integer> partBList){
		
		int last = -1;
		
        int vNum = G.length;
        if(vNum <= 0) return -1;

        int firstValid = -1;
        for(int i = 0; i < vNum; i++){
            if(vaildVs[i]) {
                firstValid = i;
                break;
            }
        }
        int vaildNum = 0;
        for(boolean b : vaildVs){
            if(b) vaildNum ++;
        }
        if(vaildNum <= 1) return -1;

        int z2 = vaildNum;
        int[] V = new int[vNum];    // v[i]代表节点i合并到的顶点
        int[] w;    // 定义w(A,x) = ∑w(v[i],x)，v[i]∈A
        boolean[] visited;    //用来标记是否该点加入了A集合
        int[] seq = new int[vaildNum];//记录移除节点的次序
        int r = 0; //记录seq的下标
        int index = -1;
        int z = vNum;
        int minCut = Integer.MAX_VALUE;

        for (int i = 0; i < vNum; i++){
            V[i] = i;  //初始还未合并，都代表节点本身
        }

        while(vaildNum > 1){
            int pre = firstValid;    //pre用来表示之前加入A集合的点（在t之前一个加进去的点）
            visited = new boolean[z];
            visited[firstValid] = true;
            w = new int[z];
            for (int i = 1; i < vaildNum; i++) {//求出某一轮最大生成树的最后两个节点，并且去除最后的t，将与t连接的边归并
                int k = -1;
                for(int j = 0; j < vNum; j++){//选取V-A中的w(A,x)最大的点x加入集合
                    if(vaildVs[V[j]] && !visited[V[j]] && V[j] != V[pre]){
                        w[V[j]] += G[V[pre]][V[j]];
                        if(k == -1 || w[V[k]] < w[V[j]]){
                            k = j;
                        }
                    }
                }
                visited[V[k]] = true;  //标记该点x已经加入A集合
                if(i == vaildNum - 1){  //若|A|=|V|（所有点都加入了A），结束
                    int s = V[pre], t = V[k];  //令倒数第二个加入A的点（v[pre]）为s，最后一个加入A的点（v[k]）为t
                    System.out.println(t + " -------> " + s);
                    seq[r] = t;
                    r ++;
                    last = s;
                    if(w[t] < minCut){//则s-t最小割为w(A,t)，用其更新min_cut
                        minCut = w[t];
                        index = r;
                    }
                    for(int j = 0; j <z; j++){//合并s,t
                        if(vaildVs[V[j]] && V[j] != s && V[j] != t){
                            G[s][V[j]] += G[V[j]][t];
                            G[V[j]][s] += G[V[j]][t];
                        }
                    }
//                    for(int p = 0; p < z; p++){
//                        for(int q = p+1; q < z; q++){
//                            if(G[p][q] != 0){
//                                System.out.println("(" + p + " , " + q + ") = " + G[p][q]);
//                            }
//                        }
//                    }
                    vaildNum --;
                    vNum --;
                    V[k] = V[vNum]; //删除最后一个点（即删除t，也即将t合并到s）
                }
                //else继续
                pre = k;
            }
        }
        seq[r] = last;
        System.out.println("最小割值为：" + minCut);
        for (int i = 0; i < z2; i++) {
            if(i < index){
            	
            	partAList.add(seq[i]);
            } else {
            	
            	partBList.add(seq[i]);
            }
        }
        
        return minCut;
    }


    public static int stoerWagner(int[][] G, int vNum, List<Integer> partAList, List<Integer> partBList){
        int[] V = new int[vNum];    // v[i]代表节点i合并到的顶点
        int[] w;    // 定义w(A,x) = ∑w(v[i],x)，v[i]∈A
        boolean[] visited;    //用来标记是否该点加入了A集合
        int[] seq = new int[vNum];//记录移除节点的次序
        int r = 0; //记录seq的下标
        int index = -1;
        int z = vNum;
        int minCut = Integer.MAX_VALUE;

        for (int i = 0; i < vNum; i++){
            V[i] = i;  //初始还未合并，都代表节点本身
        }

        while(vNum > 1){
            int pre = 0;    //pre用来表示之前加入A集合的点（在t之前一个加进去的点）
            visited = new boolean[z];
            w = new int[z];
            for (int i = 1; i < vNum; i++) {//求出某一轮最大生成树的最后两个节点，并且去除最后的t，将与t连接的边归并
                int k = -1;
                for(int j = 1; j < vNum; j++){//选取V-A中的w(A,x)最大的点x加入集合
                    if(!visited[V[j]]){
                        w[V[j]] += G[V[pre]][V[j]];
                        if(k == -1 || w[V[k]] < w[V[j]]){
                            k = j;
                        }
                    }
                }
                visited[V[k]] = true;  //标记该点x已经加入A集合
                if(i == vNum-1){  //若|A|=|V|（所有点都加入了A），结束
                    int s = V[pre], t = V[k];  //令倒数第二个加入A的点（v[pre]）为s，最后一个加入A的点（v[k]）为t
//                    System.out.println(t + " -------> " + s);
                    seq[r] = t;
                    r ++;
                    if(w[t] < minCut){//则s-t最小割为w(A,t)，用其更新min_cut
                        minCut = w[t];
                        index = r;
                    }
                    for(int j = 0; j <z; j++){//合并s,t
                        G[s][V[j]] += G[V[j]][t];
                        G[V[j]][s] += G[V[j]][t];
                    }
//                    for(int p = 0; p < z; p++){
//                        for(int q = p+1; q < z; q++){
//                            if(G[p][q] != 0){
//                                System.out.println("(" + p + " , " + q + ") = " + G[p][q]);
//                            }
//                        }
//                    }
                    vNum --;
                    V[k] = V[vNum]; //删除最后一个点（即删除t，也即将t合并到s）
                }
                //else继续
                pre = k;
            }
        }
        System.out.println("最小割值为：" + minCut);
        for (int i = 0; i < z; i++) {
            if(i < index){
            	
            	partAList.add(seq[i]);
            } else {
            	
            	partBList.add(seq[i]);
            }
        }
        
        return minCut;
    }
    
    public static int[][] mockGraph3() {
    	
    	int graph[][] = new int[10][10];
		 
//		 graph[0][1] = graph[1][0] = 2;
		 graph[1][2] = graph[2][1] = 2;
		 graph[2][3] = graph[3][2] = 2;
		 graph[3][4] = graph[4][3] = 2;
		 
//		 graph[0][5] = graph[5][0] = 1;		 
		 graph[5][6] = graph[6][5] = 1;
		 graph[6][7] = graph[7][6] = 1;
		 graph[7][8] = graph[8][7] = 1;
		 graph[7][9] = graph[9][7] = 1;
		 
//		 graph[4][8] = graph[8][4] = 1;
//		 graph[8][9] = graph[9][8] = 1;
		 
		 return graph;
	 }
    
    public static int[][] mockGraph2() {
    	
    	int graph[][] = new int[9][9];
		 
		 graph[0][1] = graph[1][0] = 2;
		 graph[1][2] = graph[2][1] = 2;
		 graph[2][3] = graph[3][2] = 2;
		 
		 graph[4][5] = graph[5][4] = 1;
		 graph[5][6] = graph[6][5] = 1;
		 graph[6][7] = graph[7][6] = 1;
		 graph[6][8] = graph[8][6] = 1;
		 
		 return graph;
	 }

    public static void main(String[] args) {
        int G[][] = new int[9][9];
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
        
        int graph[][] = mockGraph2();
        
        GraphDivisionStoerWagner sw = new GraphDivisionStoerWagner();
        List<Integer> partAList = new ArrayList<Integer>();
        List<Integer> partBList = new ArrayList<Integer>();
//        sw.stoerWagner(graph, 9, partAList, partBList);
        
        boolean[] mark = new boolean[9];
        
        for (int i = 0; i < 9; i++) {
        	
        	mark[i] = false;
        	
        }
        
        mark[5] = true;
        mark[6] = true;
        mark[3] = true;
        
        sw.stoerWagner2(G, mark, partAList, partBList);
        
        System.out.print("A部分：");
        for (Integer a : partAList) {
        	
        	System.out.print(a + "  ");
        	
        }
        System.out.println();
        System.out.print("B部分：");
        for (Integer a : partBList) {
        	
        	System.out.print(a + "  ");
        	
        }
    }
}
