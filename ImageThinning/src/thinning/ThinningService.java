package thinning;
 
import ij.IJ;

import java.util.LinkedList;
import java.util.List;
 
/**
 *
 * @author nayef
 */
public class ThinningService {
 
	public void apply_element(int[][] binaryImage){

		IJ.log("Change h "+ binaryImage.length);
		IJ.log("Change h "+ binaryImage[0].length);
		
 
        LinkedList<Point> pointsToChange = new LinkedList<Point>();
        boolean hasChange;
        int k =1;
 
        do {
        	int change =0;
            ////////////////////////case1////////////////////
            hasChange = false;
            for (int x = 1; x + 1 < binaryImage.length; x++) {
                for (int y = 1; y + 1 < binaryImage[x].length; y++) {
                	change++;
                    if ( binaryImage[x][y]==1
                            && (binaryImage[x - 1][y-1]==0 && binaryImage[x][y-1]==0 && binaryImage[x+1][y - 1] == 0)
                            && (binaryImage[x-1][y + 1]==1 && binaryImage[x][y+1] ==1 && binaryImage[x+1][y+ 1] == 1)) {
                        //pointsToChange.add(new Point(x, y));
                        binaryImage[x][y] = 0;
                        hasChange = true;
                    }
                }
            }
            //change = change + pointsToChange.size()
            //IJ.log("Change size "+change);
           /* for (Point point:pointsToChange) {
                binaryImage[point.getY()][point.getX()] = 0;
                //IJ.log("X =" +point.getX());
            }*/
 
            pointsToChange.clear();
           ///////////////////////////////case5///////////////////////////
            
            for (int x = 1; x + 1 < binaryImage.length; x++) {
                for (int y = 1; y + 1 < binaryImage[x].length; y++) {
                   
                    if ( binaryImage[x][y]==1
                            && (binaryImage[x][y-1]==0 && binaryImage[x+1][y - 1] == 0 
                            && binaryImage[x-1][y] == 1)&& (binaryImage[x+1][y]==0 && 
                            binaryImage[x][y+1] ==1)) {
                        //pointsToChange.add(new Point(x, y));
                        binaryImage[x][y] = 0;
                        hasChange = true;
                    }
                }
            }
            //IJ.log("Change size 2 "+ pointsToChange.size());
            /*for (Point point:pointsToChange) {
                binaryImage[point.getY()][point.getX()] = 0;
            }*/
 
            pointsToChange.clear();
            ////////////////////////////case2//////////////////////////
            
            for (int x = 1; x + 1 < binaryImage.length; x++) {
                for (int y = 1; y + 1 < binaryImage[x].length; y++) {
                   
                    if ( binaryImage[x][y]==1
                            && (binaryImage[x - 1][y-1]==1 && binaryImage[x+1][y - 1] == 0)
                            && (binaryImage[x-1][y]==1 && binaryImage[x+1][y] ==0 && binaryImage[x-1][y+ 1] == 1)
                            && binaryImage[x+1][y+1]==0) {
                        //pointsToChange.add(new Point(x, y));
                        binaryImage[x][y] = 0;
                        hasChange = true;
                    }
                }
            }
            //IJ.log("Change size3 "+ pointsToChange.size());
            /*for (Point point:pointsToChange) {
                binaryImage[point.getY()][point.getX()] = 0;
            }*/
 
            pointsToChange.clear();
            ///////////////////////////////case3////////////////////////////
            
            for (int x = 1; x + 1 < binaryImage.length; x++) {
                for (int y = 1; y + 1 < binaryImage[x].length; y++) {
                   
                    if ( binaryImage[x][y]==1
                            && (binaryImage[x - 1][y-1]==1 && binaryImage[x][y - 1] == 1 
                            && binaryImage[x+1][y - 1] == 1)&& (binaryImage[x-1][y+1]==0 && 
                            binaryImage[x][y+1] ==0 && binaryImage[x+1][y+ 1] == 0)) {
                        //pointsToChange.add(new Point(x, y));
                        binaryImage[x][y] = 0;
                        hasChange = true;
                    }
                }
            }
           // IJ.log("Change size4 "+ pointsToChange.size());
            /*for (Point point:pointsToChange) {
                binaryImage[point.getY()][point.getX()] = 0;
            }*/
 
            pointsToChange.clear();
            
            ////////////////////////////////case4///////////////////////////
            
            for (int x = 1; x + 1 < binaryImage.length; x++) {
                for (int y = 1; y + 1 < binaryImage[x].length; y++) {
                   
                    if ( binaryImage[x][y]==1
                            && (binaryImage[x - 1][y-1]==0 && binaryImage[x+1][y - 1] == 1 
                            && binaryImage[x-1][y] == 0)&& (binaryImage[x+1][y]==1 && 
                            binaryImage[x-1][y+1] ==0 && binaryImage[x+1][y+ 1] == 1)) {
                       // pointsToChange.add(new Point(x, y));
                        binaryImage[x][y] = 0;
                        hasChange = true;
                    }
                }
            }
            //IJ.log("Change size5 "+ pointsToChange.size());
            /*for (Point point:pointsToChange) {
                binaryImage[point.getY()][point.getX()] = 0;
            }*/
 
            pointsToChange.clear();
 
            //////////////////////////////case6//////////////////////////////
            
            for (int x = 1; x + 1 < binaryImage.length; x++) {
                for (int y = 1; y + 1 < binaryImage[x].length; y++) {
                   
                    if ( binaryImage[x][y]==1
                            && (binaryImage[x][y-1]==1 && binaryImage[x-1][y] == 1 
                            && binaryImage[x+1][y] == 0)&& (binaryImage[x][y+1]==0 && 
                            binaryImage[x+1][y+1] ==0)) {
                       // pointsToChange.add(new Point(x, y));
                        binaryImage[x][y] = 0;
                        hasChange = true;
                    }
                }
            }
           // IJ.log("Change size6 "+ pointsToChange.size());
            /*for (Point point:pointsToChange) {
                binaryImage[point.getY()][point.getX()] = 0;
            }*/
 
            pointsToChange.clear();
            //////////////////////////////////case7////////////////////////////
            
            for (int x = 1; x + 1 < binaryImage.length; x++) {
                for (int y = 1; y + 1 < binaryImage[x].length; y++) {
                   
                    if ( binaryImage[x][y]==1
                            && (binaryImage[x][y-1]==1 && binaryImage[x-1][y] == 0 
                            && binaryImage[x+1][y] == 1)&& (binaryImage[x-1][y+1]==0 && 
                            binaryImage[x][y+1] ==0)) {
                        //pointsToChange.add(new Point(x, y));
                        binaryImage[x][y] = 0;
                        hasChange = true;
                    }
                }
            }
            //IJ.log("Change size7 "+ pointsToChange.size());
            /*for (Point point:pointsToChange) {
                binaryImage[point.getY()][point.getX()] = 0;
            }*/
 
            pointsToChange.clear();
            ///////////////////////////case8////////////////
           
            for (int x = 1; x + 1 < binaryImage.length; x++) {
                for (int y = 1; y + 1 < binaryImage[x].length; y++) {
                   
                    if ( binaryImage[x][y]==1
                            && (binaryImage[x-1][y-1]==0 && binaryImage[x][y - 1] == 0 
                            && binaryImage[x-1][y] == 0)&& (binaryImage[x+1][y]==1 && 
                            binaryImage[x][y+1] ==1)) {
                        //pointsToChange.add(new Point(x, y));
                        binaryImage[x][y] = 0;
                        hasChange = true;
                    }
                }
            }
            /*for (Point point:pointsToChange) {
                binaryImage[point.getY()][point.getX()] = 0;
            }*/
 
            pointsToChange.clear();
            ///////////////////////end////////////////////
            k --;
        } while (hasChange);
 
       
    
	}
   
 
    private class Point {
 
        private int x, y;
 
        public Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
 
        public int getX() {
            return x;
        }
 
        public void setX(int x) {
            this.x = x;
        }
 
        public int getY() {
            return y;
        }
 
        public void setY(int y) {
            this.y = y;
        }
    };
 
}