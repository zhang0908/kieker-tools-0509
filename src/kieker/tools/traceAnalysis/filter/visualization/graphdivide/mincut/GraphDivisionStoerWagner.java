package kieker.tools.traceAnalysis.filter.visualization.graphdivide.mincut;


import java.util.ArrayList;
import java.util.List;

/**
 * 1.min=MAXINT���̶�һ������P
 *
 * 2.�ӵ�P�á����ơ�prim��s�㷨��չ�������������������¼�����չ�Ķ���������չ�ı�
 *
 * 3.���������չ���Ķ�����и�ֵ������˶������������б�Ȩ�ͣ�������minС����min
 *
 * 4.�ϲ������չ�������ߵ������˵�Ϊһ�����㣨��Ȼ���ǵı�ҲҪ�ϲ�����������ɣ���
 *
 * 5.ת��2���ϲ�N-1�κ����
 *
 * 6.min��Ϊ�������min
 *
 * ��prim�����Ӷ���O(n^2)���ϲ�n-1�Σ��㷨���Ӷȼ�ΪO(n^3)�������prim�мӶ��Ż������ӶȻήΪO((n^2)logn)
 *
 * ��������⣨reference http://www.cnblogs.com/ihopenot/p/5986772.html����
 * ��������������Ϊs��t�����ȫ����С����s��t����һ�������У���ô��Ȼȫ����С�Ϊs-t��С�
 * �������ǽ�s��t����һ���ڵ���ڴ���û��Ӱ��ġ�������һ�㣬ÿ�ν������ģ��С����⡣
 * һ��ʼѡ��Ľڵ�����Ϊs-t���м�ڵ㼯����Ϊÿ����չ��ѡȡ��ϵ�����ĵ���չ��
 * �����м�ڵ㼯�е㻥������ϵ���Ǵ���st���м�㼯����ϵ�ȵģ���������ĵ�t����ϵ������С�ģ�
 * ������С�Ϊ��������ϵ�ȣ���Ϊsͨ���м�ڵ㼯��t����������sֱ�ӵ�t�����������Ծ�֤����ÿ����չ�������s-t����С�
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
        int[] V = new int[vNum];    // v[i]����ڵ�i�ϲ����Ķ���
        int[] w;    // ����w(A,x) = ��w(v[i],x)��v[i]��A
        boolean[] visited;    //��������Ƿ�õ������A����
        int[] seq = new int[vaildNum];//��¼�Ƴ��ڵ�Ĵ���
        int r = 0; //��¼seq���±�
        int index = -1;
        int z = vNum;
        int minCut = Integer.MAX_VALUE;

        for (int i = 0; i < vNum; i++){
            V[i] = i;  //��ʼ��δ�ϲ���������ڵ㱾��
        }

        while(vaildNum > 1){
            int pre = firstValid;    //pre������ʾ֮ǰ����A���ϵĵ㣨��t֮ǰһ���ӽ�ȥ�ĵ㣩
            visited = new boolean[z];
            visited[firstValid] = true;
            w = new int[z];
            for (int i = 1; i < vaildNum; i++) {//���ĳһ���������������������ڵ㣬����ȥ������t������t���ӵı߹鲢
                int k = -1;
                for(int j = 0; j < vNum; j++){//ѡȡV-A�е�w(A,x)���ĵ�x���뼯��
                    if(vaildVs[V[j]] && !visited[V[j]] && V[j] != V[pre]){
                        w[V[j]] += G[V[pre]][V[j]];
                        if(k == -1 || w[V[k]] < w[V[j]]){
                            k = j;
                        }
                    }
                }
                visited[V[k]] = true;  //��Ǹõ�x�Ѿ�����A����
                if(i == vaildNum - 1){  //��|A|=|V|�����е㶼������A��������
                    int s = V[pre], t = V[k];  //����ڶ�������A�ĵ㣨v[pre]��Ϊs�����һ������A�ĵ㣨v[k]��Ϊt
                    System.out.println(t + " -------> " + s);
                    seq[r] = t;
                    r ++;
                    last = s;
                    if(w[t] < minCut){//��s-t��С��Ϊw(A,t)���������min_cut
                        minCut = w[t];
                        index = r;
                    }
                    for(int j = 0; j <z; j++){//�ϲ�s,t
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
                    V[k] = V[vNum]; //ɾ�����һ���㣨��ɾ��t��Ҳ����t�ϲ���s��
                }
                //else����
                pre = k;
            }
        }
        seq[r] = last;
        System.out.println("��С��ֵΪ��" + minCut);
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
        int[] V = new int[vNum];    // v[i]����ڵ�i�ϲ����Ķ���
        int[] w;    // ����w(A,x) = ��w(v[i],x)��v[i]��A
        boolean[] visited;    //��������Ƿ�õ������A����
        int[] seq = new int[vNum];//��¼�Ƴ��ڵ�Ĵ���
        int r = 0; //��¼seq���±�
        int index = -1;
        int z = vNum;
        int minCut = Integer.MAX_VALUE;

        for (int i = 0; i < vNum; i++){
            V[i] = i;  //��ʼ��δ�ϲ���������ڵ㱾��
        }

        while(vNum > 1){
            int pre = 0;    //pre������ʾ֮ǰ����A���ϵĵ㣨��t֮ǰһ���ӽ�ȥ�ĵ㣩
            visited = new boolean[z];
            w = new int[z];
            for (int i = 1; i < vNum; i++) {//���ĳһ���������������������ڵ㣬����ȥ������t������t���ӵı߹鲢
                int k = -1;
                for(int j = 1; j < vNum; j++){//ѡȡV-A�е�w(A,x)���ĵ�x���뼯��
                    if(!visited[V[j]]){
                        w[V[j]] += G[V[pre]][V[j]];
                        if(k == -1 || w[V[k]] < w[V[j]]){
                            k = j;
                        }
                    }
                }
                visited[V[k]] = true;  //��Ǹõ�x�Ѿ�����A����
                if(i == vNum-1){  //��|A|=|V|�����е㶼������A��������
                    int s = V[pre], t = V[k];  //����ڶ�������A�ĵ㣨v[pre]��Ϊs�����һ������A�ĵ㣨v[k]��Ϊt
//                    System.out.println(t + " -------> " + s);
                    seq[r] = t;
                    r ++;
                    if(w[t] < minCut){//��s-t��С��Ϊw(A,t)���������min_cut
                        minCut = w[t];
                        index = r;
                    }
                    for(int j = 0; j <z; j++){//�ϲ�s,t
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
                    V[k] = V[vNum]; //ɾ�����һ���㣨��ɾ��t��Ҳ����t�ϲ���s��
                }
                //else����
                pre = k;
            }
        }
        System.out.println("��С��ֵΪ��" + minCut);
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
        
        System.out.print("A���֣�");
        for (Integer a : partAList) {
        	
        	System.out.print(a + "  ");
        	
        }
        System.out.println();
        System.out.print("B���֣�");
        for (Integer a : partBList) {
        	
        	System.out.print(a + "  ");
        	
        }
    }
}
