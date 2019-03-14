//Given a grid, where cells either are empty or have a letter, 
//fill in the empty cells with the letter closest to that cell by Manhattan distance. 
//Cells that have letters that are equally close can break ties in any manner.
//
//Input (as 2D array):
//=============
//A |   |   | 
// |   |   |
// |   | B | 
//============= 
//
//Output:
//=============
//A | A  | B | 
//A | A  | B |
//A | B  | B | 
//=============
import java.util.*;
import java.util.Map;
import java.util.HashMap;

public class FillMatrixWithClosestCharacters {
    
    public static void main(String args[] ) throws Exception {
        FillMatrixWithClosestCharacters solution  = new FillMatrixWithClosestCharacters();
//        char[][] input = new char[][]{{'A',' ',' '}, {' ', ' ', ' '}, {' ', ' ', 'B'}};
//        char[][] output = solution.fillMatrix(input);
        
        char[][] input = new char[][]{{'A',' ',' ','C',' '}, {' ', ' ', ' ',' ',' '}, {' ', ' ', 'B',' ',' '}, {'A',' ',' ','C',' '}, {'A',' ',' ','C',' '}};
        char[][] output = solution.fillMatrix(input);
        for(int i=0;i<output.length;i++){
            for(int j=0;j<output[0].length;j++){
                System.out.print(output[i][j]);
            }
            System.out.println();
        }
    }
    
    private char[][] fillMatrix(char[][] input){
        if(input==null) {
            return null;
        }
        int m = input.length;
        int n = input[0].length;
        char[][] output = new char[m][n];
        
        //create boundary for each non empty cell, which use the middle point between pair of the non empty cell. 
        //This should take O(mn) time if the number of non empty cell (k=list.size()) is not more than sqrt(mn)
        //If the number of non empty cell is more, then we should consider to cluster the non empty cells in different 
        //zone to reduce the time to populate the boundary for each non empty cell.
        Map<String, Boundary> boundaryMap = new HashMap<>();
        List<int[]> list = new ArrayList<>();
        for(int i=0;i<input.length;i++){
            for(int j=0;j<input[0].length;j++){
                if(input[i][j]!=' '){
                  list.add(new int[]{i,j});    //get all the non empty cell
                }
            }
        }
        for(int i=0;i<list.size();i++){
          int[] point = list.get(i);
          Boundary boundary = new Boundary();
          for(int j=0;j<list.size();j++){
            if(j==i)  continue;
            int[] otherP = list.get(j);
            int nx = (point[0]+otherP[0])/2;
            int ny = (point[1]+otherP[1])/2;
            addBoundary(nx, ny, point[0], point[1], boundary);   //add boundary for each pair of non empty cells
          }
          boundaryMap.put(point[0]+","+point[1], boundary);   //associate the final boundary to the original point
        }
        
        //for each non empty cell do a breath first search within the boundary, since all these boundaries should have 
        //very limited overlaps, we can consider overall the time complexity here is O(mn)
        Map<String, Integer> dist = new HashMap<String, Integer>();
        for(int i=0;i<input.length;i++){
            for(int j=0;j<input[0].length;j++){
                if(input[i][j]!=' '){
                    fill(input, i, j, dist, output, boundaryMap);
                    output[i][j] = input[i][j];
                }
            }
        }
        return output;
    }
    
    private void fill(char[][] input, int x, int y, Map<String, Integer> dist, char[][] output, Map<String, Boundary> boundaryMap){
        int[][] dr = new int[][] {{0,1},{0,-1},{1,0},{-1,0}};
        Boundary boundary = boundaryMap.getOrDefault(x+"."+y, new Boundary());
        Queue<int[]> queue = new LinkedList<>();
        queue.offer(new int[] {x,y});
        int distance = 0;
        boolean[][] visited = new boolean[input.length][input[0].length];
        while(!queue.isEmpty()) {
            int size = queue.size();
            boolean flag = false;
            distance++;
            for(int i=0;i<size;i++) {
                int[] point = queue.poll();
                visited[point[0]][point[1]]=true;
                for(int j=0;j<dr.length;j++) {
                    int nx = point[0]+dr[j][0];
                    int ny = point[1]+dr[j][1]; 
                    if(nx<0||nx>=input.length||ny<0||ny>=input[0].length||visited[nx][ny]) {
                        continue;
                    }
                    if(input[nx][ny]!=' ') {
                        addBoundary(nx, ny, x, y, boundary);
                    }else {
                        if(!inBoundary(nx, ny, boundary)) {
                            continue;
                        }   
                        if(!dist.containsKey(nx+","+ny)||distance<dist.get(nx+","+ny)){
                            dist.put(nx+","+ny, distance);
                            output[nx][ny] = input[x][y];
                            flag = true;
                        }
                        queue.offer(new int[] {nx, ny});
                    }
                }
            }
            if(!flag) {
                break;
            }
        }

    }
    
    //add boundary for original point(x,y) according to new point(nx, ny) and boundary itself 
    private void addBoundary(int nx, int ny, int x, int y, Boundary boundary) {
        if(nx<x&&ny<y&&(nx>boundary.topLeft[0]&&ny>boundary.topLeft[1])) {
            boundary.topLeft=new int[] {nx, ny};
        }else if(nx<x&&ny>y&&(nx>boundary.bottomLeft[0]&&ny<boundary.bottomLeft[1])) {
            boundary.bottomLeft=new int[]{nx, ny};
        }else if(nx>x&&ny<y&&(nx<boundary.topRight[0]&&ny>boundary.topRight[1])) {
            boundary.topRight=new int[] {nx, ny};
        }else if(nx>x&&ny>y&&(nx<boundary.bottomRight[0]&&ny<boundary.bottomRight[1])) {
            boundary.bottomRight=new int[] {nx, ny};
        }else if(nx<x&&ny==y&&nx>boundary.left) {
            boundary.left=nx;
        }else if(nx>x&&ny==y&&nx<boundary.right) {
            boundary.right=ny;
        }else if(nx==x&&ny<y&&ny>boundary.top) {
            boundary.top=y;
        }else if(nx==x&&ny>y&&ny<boundary.bottom) {
            boundary.bottom=y;
        }
    }
    
    
    //check if a coordinate is inside a boundary (x,y)
    private boolean inBoundary(int x, int y, Boundary boundary) {
        if(x<boundary.left||x>boundary.right||y>boundary.bottom||y<boundary.top
                ||(x<boundary.topLeft[0]&&y<boundary.topLeft[1])
                ||(x>boundary.topRight[0]&&y<boundary.topRight[1])
                ||(x<boundary.bottomLeft[0]&&y>boundary.bottomLeft[1])
                ||(x>boundary.bottomRight[0]&&y>boundary.bottomRight[1])) {
            return false;
        }
        return true;
    }
    
    class Boundary{
        int left=Integer.MIN_VALUE;
        int right=Integer.MAX_VALUE;
        int top=Integer.MIN_VALUE;
        int bottom=Integer.MAX_VALUE;
        int[] topLeft=new int[] {Integer.MIN_VALUE, Integer.MIN_VALUE};
        int[] topRight=new int[] {Integer.MAX_VALUE, Integer.MIN_VALUE};
        int[] bottomLeft=new int[] {Integer.MIN_VALUE, Integer.MAX_VALUE};
        int[] bottomRight=new int[] {Integer.MAX_VALUE, Integer.MAX_VALUE};
        
    }
}
