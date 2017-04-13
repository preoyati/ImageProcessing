package thinning;
 
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.ZProjector;
import ij.plugin.filter.EDM;
//import ij.io.FileSaver;
//import ij.plugin.filter.GaussianBlur;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;
//import ij.process.BinaryProcessor;



import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
/*import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;*/
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;

import skeleton_analysis.AnalyzeSkeleton_;

//import Coordinates;

//import coordinates.Coordinates;


//import coordinates.Coordinates;

//import javax.imageio.ImageIO;

//import thinning.thinning.WaitCoord;

//import coordinates.Coordinates;

//import microvesselAnalysis.TDRgrowing.TDRgrowing;
 
/**
 *
 * @author nayef
 */
public class thinning implements PlugInFilter {
 
    /**
     * @param args the command line arguments
     */
	
	
	int width_all[];
	double length_all[];
	public class WaitCoord{
		Coordinates cord;
		int color;
		WaitCoord(int x, int y, int c){
			cord = new Coordinates(x, y);
			color =c;	
		}
	}
	public static class Region{
		int colorID;
		LinkedList<Coordinates> PixelsCoords;
		Region(LinkedList<Coordinates> list, int CID){
			this.colorID = CID;
			this.PixelsCoords = new LinkedList<Coordinates>();
			this.PixelsCoords.addAll(list);
		}
		Region(){this.colorID =0; this.PixelsCoords = new LinkedList<Coordinates>();}
	}
	
	LinkedList<WaitCoord> FIFOList = new LinkedList<WaitCoord>();
	 private static HashSet<Coordinates> set =new HashSet<Coordinates>();
	public ImagePlus imp;
	static String Title;
	int height, width;
	int Colors[];
	int BoarderColor;
	int totalVoxel=0;
	static int Thresholdlow;
	static int ThresholdHigh;
	ResultsTable Whole_RT=new ResultsTable();
	ResultsTable Objects_RT=new ResultsTable();

	
	@SuppressWarnings("null")
	ImageProcessor globalobjectIP;
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		thinning ob =new thinning();
		//ImagePlus im = IJ.openImage("C:\\Image\\Microvessel-B.tif");
		ImagePlus im = IJ.openImage("G:\\IMAGE_PROCESSING\\now\\Microvessel_3D_preprocessed.tif");
				
		ob.imp =im;
		im.show();
		
		
		//String ProIMP;
		//IJ.run("Z Project...");
		
		ob.run(ob.imp.getProcessor());
		IJ.log("Done");
		
	}
	
	
	
	private static void ExtractFullObject(ImageProcessor projected_Edge_IP, ImageProcessor objectIP) {
		// TODO Auto-generated method stub
		
		ImagePlus FinImp = new ImagePlus("Objects",objectIP);
		FinImp.show();
		FinImp.updateAndDraw();
		
		int w = projected_Edge_IP.getWidth();
		int h = projected_Edge_IP.getHeight();
		int flag =1;
		while(flag==1){
			objectIP.erode();
			FinImp.show();
			FinImp.updateAndDraw();
			
			for(int j=0; j<h;j++)
				for(int i =0; i<w;i++){
					if(projected_Edge_IP.getPixel(i, j)>0&&objectIP.getPixel(i, j)>0)
						flag =0;
				}
					
			
		}
		
	}

	private static void FindObjects(LinkedList<Region> objectList, ByteProcessor objip) {
		// TODO Auto-generated method stub
		LinkedList<Coordinates> Coordlist = new LinkedList<Coordinates>();	
		Region R;
		ByteProcessor Dup_oobjip = (ByteProcessor) objip.duplicate();
		Thresholdlow =240;
	    ThresholdHigh = 255;
		int i=0,j=0, found =0, px;
		int h = objip.getHeight();
	    int w = objip.getWidth();
		for ( j =0; j< h && found==0; j++)
			for( i  =0; i<w; i++){
				if(objip.getPixel(i, j)==255){
					objip.putPixel(i, j, 250);
					Dup_oobjip.putPixel(i, j, 250);
				}	
			}
	    while(true){
			found =0;
			
			for ( j =0; j< h && found==0; j++)
				for( i  =0; i<w; i++)
				{
				    if (Dup_oobjip.getPixel(i, j)==250){
				    	found =1;
				    	break;
				    }
				}
			if (i==w||j== h)
			{
				IJ.log("No more objects found");
				break;
			}
			Coordlist.clear();
			floodFill(Coordlist, objip,Dup_oobjip,new Coordinates(i,j));
			R = new Region(Coordlist, 0);
			objectList.add(R);
			
		}  
	}

	@Override
	public void run(ImageProcessor ip) {
		// TODO Auto-generated method stub
		//ip.bin(shrinkFactor)
	   
		width_all =new int[100];
		length_all = new double[100];
		
		VolumeSurface(ip);
		// Here Do preprocessings and generate thresholded Image 
	    ImageProcessor PreProcessedIP = ip.duplicate();
	    ImageStack ObjectStack = imp.createEmptyStack();
		GenerateFullObjectList(ObjectStack, imp);
		ImagePlus ObjectStackImage =new ImagePlus("Object Stack",ObjectStack);
		ObjectStackImage.show();
		ObjectStackImage.updateAndDraw();
		for(int s=1;s<= ObjectStack.getSize();s++){
			ByteProcessor b1=(ByteProcessor)ObjectStack.getProcessor(s).duplicate();
			
			CalculateLengthWidth(b1,s-1);
		}
		Objects_RT.reset();
		for(int t=0;t<ObjectStack.getSize();t++){
			Objects_RT.incrementCounter();
			Objects_RT.addValue("Width", width_all[t]);
			Objects_RT.addValue("length", length_all[t]);
			
			
		}
		Objects_RT.show("Object List");
		//ImagePlus ObjectImp = new ImagePlus("All object Image",objectStack);
		ZProjector zp = new ZProjector(ObjectStackImage);
		zp.setMethod(ZProjector.MAX_METHOD);
		//zp.setImage(im);
		zp.doProjection();
		ImagePlus ProjectedImp = zp.getProjection();
		ProjectedImp.show();
		globalobjectIP = ProjectedImp.getProcessor();
		//ProjectedIP.sh
		//ColorMajorObjects(StackColored,ObjectStack);
	    //
	    
		initcolors();
		height = ip.getHeight();
		width = ip.getWidth();
		//int [][]Values = new int[width][height];
		ImageStack stack = imp.getStack();
		ImageStack SkeletonBH = imp.createEmptyStack();
		ImageStack SkeletonColor = imp.createEmptyStack();
		ImageStack StackColored = imp.createEmptyStack();
		ColorProcessor Prev_orgCp;
	    int st =1;
		int stacksize = stack.getSize();
		while(st<=stacksize){
			ByteProcessor B = (ByteProcessor)stack.getProcessor(st).duplicate();
			B.invert();
			B.skeletonize();
			ByteProcessor DupB = (ByteProcessor) B.duplicate();
			ColorProcessor cp = (ColorProcessor) B.convertToRGB();
			ColorProcessor orgCp=(ColorProcessor) stack.getProcessor(st).duplicate().convertToRGB();
			
			if(st>1)
			Prev_orgCp = (ColorProcessor) StackColored.getProcessor(StackColored.getSize());
			else
				Prev_orgCp =null;
			//ImagePlus im1 =new ImagePlus("colored_skeleton",cp);
			ImagePlus imorg =new ImagePlus("colored_skeleton",orgCp);
			
			int i=0,j=0, found =0;
			while(true){
				found =0;
				
				for ( j =0; j< DupB.getHeight()&& found==0; j++)
					for( i  =0; i<DupB.getWidth(); i++)
					{
						//IJ.log
					    if (DupB.getPixel(i, j)==0){
					    	found =1;
					    	break;
					    }
					    	//cp.set(i,j,Colors[2]);
					}
				if (i==DupB.getWidth()||j== DupB.getHeight())
				{
					IJ.log("No black Pixels found");
					break;
				}
				LinkedList<Coordinates> branchCoords =  ColorBranches(Prev_orgCp, orgCp,cp, DupB,i,j-1);	
				//LinkedList<Coordinates> BoundaryCoordinates = FindBoundary(stack.getProcessor(st).duplicate(),i,j-1);
				
				imorg.show();
				imorg.updateAndDraw();
			    try {
					ColorGrowing(imorg,orgCp,stack.getProcessor(st),branchCoords);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			imorg.close();
			SkeletonBH.addSlice(B);
			SkeletonColor.addSlice(cp);
			StackColored.addSlice(orgCp);
	
			st++;
		}
				
		
		ImagePlus SkelBH =new ImagePlus("Black and White Skeleton",SkeletonBH);
		SkelBH.show();
		SkelBH.updateAndDraw();
		ImagePlus SkelColor =new ImagePlus("Colored Skeleton",SkeletonColor);
		SkelColor.show();
		SkelColor.updateAndDraw();
		ImagePlus ColoredImage =new ImagePlus("Colored Image",StackColored);
		ColoredImage.show();
		ColoredImage.updateAndDraw();
		
		
		/*double Surface;
		Surface = CalculateSurface(stack);
		IJ.log("Surface area = "+Surface);*/
		
		//FileSaver FS = new FileSaver(im1);
		//FS.saveAsTiff("C:\\Image\\Microvessel-Th_color.tif");
		
	}


	/*private Double CalculateSurface(ImageStack stack) {
		// TODO Auto-generated method stub
		double surfacearea=0.0, sidearea = 1.0, toparea=1.0;
		int intensity;
		//Coordinates temp = null;
		ImageProcessor myip,myip_prev,myip_next;
		int NPS=0,NPT=0;
		for(int i=1; i<=stack.getSize();i++){
			myip=stack.getProcessor(i);
			for(int x=0;x<width;x++){
				for(int y=0;y<height;y++){
					
					intensity=myip.getPixel(x, y);
					if(intensity>=254){
						
						int neig_count=0;
						int p,q, pix;
				        for(int m=-1; m<2; m++) {
				        	for(int n=-1; n<2; n++) {
				        		p = x + m;
				        		q = y + n;
				        		//IJ.log("How");
				        		//Coordinates temp = new Coordinates(p,q );
				                if((m == 0) && (n == 0))
				                	continue;			
				                if ( p<0 || p>=width|| q<0||q>=height)
				                	continue;
				                pix=myip.getPixel(p, q);
				                //IJ.log("x = "+p+ "  y = "+q+ "  pix = "+pix );
				                if( pix>=254)				                
				                	neig_count++;          	
				                
				            }				        	
				        }
				        NPS=NPS+(8-neig_count);
				        if(i==1)
				        	NPT++;
				        else
				        {
				        	myip_prev=stack.getProcessor(i-1);
				        	if(myip_prev.getPixel(x, y)==0)       		
				        		NPT++;
				        }
				        if(i==stack.getSize()) NPT++;
				        else{
				        	myip_next=stack.getProcessor(i+1);
				        	if(myip_next.getPixel(x, y)==0)       		
				        		NPT++;
				        }
				        	
				        
						
					}
				}
				
			}
		}
		surfacearea= (double)(NPS*sidearea+NPT*toparea);
		return surfacearea;
		
	}*/

	private void ColorMajorObjects(ImageStack stackColored, ImageStack objectStack) {
		// TODO Auto-generated method stub
		LinkedList<Coordinates> ObjectCoordinates = new LinkedList<Coordinates>();
		ImagePlus ObjectImp = new ImagePlus("All object Image",objectStack);
		ZProjector zp = new ZProjector(ObjectImp);
		zp.setMethod(ZProjector.MAX_METHOD);
		//zp.setImage(im);
		zp.doProjection();
		ImagePlus ProjectedImp = zp.getProjection();
		ProjectedImp.show();
		ImageProcessor ProjectedIP = ProjectedImp.getProcessor();
		
		for(int k =1;k<=objectStack.getSize(); k++){
			ImageProcessor ObjectIP = objectStack.getProcessor(k);
			for(int j =0; j<ObjectIP.getHeight();j++)
				for(int i=0;i<ObjectIP.getWidth();i++)
					if(ObjectIP.getPixel(i, j)>0)
						ObjectCoordinates.add(new Coordinates(i,j));
		}
		
		Coordinates tempC;
		for(int k =1;k<=stackColored.getSize(); k++){
			IJ.log("test st"+k);
			ImageProcessor ColorIP = stackColored.getProcessor(k);
			for(int j =0;j<ObjectCoordinates.size();j++){
				tempC = ObjectCoordinates.get(j);
				if(ProjectedIP.getPixel(tempC.getX(), tempC.getY())>0&&ColorIP.getPixel(tempC.getX(), tempC.getY())>0)
					ColorIP.set(tempC.getX(), tempC.getY(),Colors[0]);
			}
		}
	
	}



	private void GenerateFullObjectList(ImageStack objectStack, ImagePlus imp2) {
		// TODO Auto-generated method stub
		
		
		ZProjector zp = new ZProjector(imp2);
		zp.setMethod(ZProjector.MAX_METHOD);
		//zp.setImage(im);
		zp.doProjection();
		ImagePlus ProjectedImp = zp.getProjection();
		
		
		ByteProcessor Bdmp = (ByteProcessor) ProjectedImp.getProcessor().duplicate();//.convertToByteProcessor();
		EDM dm = new EDM();
		dm.toEDM(Bdmp);
		Bdmp.autoThreshold();
		ImagePlus dmpIM = new ImagePlus("Distance map", Bdmp);
		dmpIM.show();   // Next input in file must be generated from this image
		int NumberofObject= ObjectSeparation(Bdmp,dmpIM);
		
		IJ.log("NUmber of Object in the main ImAGE "+NumberofObject);		
		ImageProcessor ProjectedIP = ProjectedImp.getProcessor();
		ProjectedIP.findEdges();
		ProjectedImp.show();
		ProjectedImp.updateAndDraw();
		
		//ImagePlus Objim = IJ.openImage("C:\\Image\\object_sep_bin.tif");
		ByteProcessor Objip = Bdmp;//(ByteProcessor) Objim.getProcessor();
		
		LinkedList<Region> objectList = new LinkedList<Region>();
		Region R;
		FindObjects(objectList,Objip);
		Region  tempR;
		if(NumberofObject>objectList.size())
			NumberofObject=objectList.size();
		
		for(int k =0; k<NumberofObject;k++)
		{
			R = objectList.get(0);
			int i =0, index=0;
			int max = R.PixelsCoords.size();
			while(i<objectList.size()){
				tempR=objectList.get(i);
				if(tempR.PixelsCoords.size()>max){
					R = tempR;
					max = tempR.PixelsCoords.size();
					index=i;
				}
				i++;
			}
			ImageProcessor ObjectIP = Objip.createProcessor(Objip.getWidth(), Objip.getHeight());
			Coordinates tempC;
			while(!R.PixelsCoords.isEmpty())
			{
				tempC = R.PixelsCoords.poll();
				ObjectIP.putPixel(tempC.getX(),tempC.getY(),255);
			}
			ExtractFullObject(ProjectedIP,ObjectIP);
			objectStack.addSlice(ObjectIP);
			objectList.remove(index);
			
		}
		/*ImagePlus FinImp = new ImagePlus("Objects",ObjectIP);
		FinImp.show();
		FinImp.updateAndDraw();*/
		
		
	}



	private int ObjectSeparation(ByteProcessor bdmp, ImagePlus dmpIM) {
		// TODO Auto-generated method stub
		
		int N=0;
		final ImageProcessor ip2=bdmp;
		final ImagePlus imp_run = new ImagePlus("Image in Run",ip2);
		ip2.autoThreshold();
		imp_run.show();
		GenericDialog gd = new GenericDialog("Test");
		gd.addMessage("Enter Erore or Dialate buttons to adjust object area\n"); 
		Button bt1 = new Button("Erode");
	    Button bt2 = new Button("Dialate");
		gd.add(bt1);
		gd.add(bt2);      
		gd.addNumericField("Enter Number of Object to extract", 1, N);
		bt1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ip2.erode();
		        imp_run.updateAndDraw();
		    }
		});
		bt2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e){
				ip2.dilate();
		        imp_run.updateAndDraw();
		    }
		});

		  // Add and show button  
		  gd.showDialog(); 
		  if (gd.wasCanceled()) return 0;
		  
		  N = (int) gd.getNextNumber();
		  
		  return N;
		
	}



	private LinkedList<Coordinates> FindBoundary(ImageProcessor duplicate,int i, int j) {
		// TODO Auto-generated method stub
		LinkedList<Coordinates> BoundaryNodes = new LinkedList<Coordinates>();
		LinkedList<Coordinates> NodeList = new LinkedList<Coordinates>();
		LinkedList<Coordinates> l = new LinkedList<Coordinates>();
		NodeList.add(new Coordinates(i,j));
		Coordinates temp=null;
		ByteProcessor B = duplicate.convertToByteProcessor();
		B.invert();
		ByteProcessor BD = (ByteProcessor) B.duplicate();
		//ByteProcessor BC;
		
		if(B.isBinary())
			IJ.log("\nYes Binary");
		ImagePlus umg = new ImagePlus ( "TestImage",B);
		umg.show();
		umg.updateAndDraw();
		
		while (!NodeList.isEmpty()){
				
			temp = NodeList.poll();
			//BC = B;
			//LinkedList<Coordinates> l = new LinkedList<Coordinates>();
			int neig_count=0;
	        int p,q, pix;
	        for(int m=-1; m<2; m++) {
	        	for(int n=-1; n<2; n++) {
	        		p = temp.getX()+m;
	        		q = temp.getY()+n;
	        		//IJ.log("How");
	        		//Coordinates temp = new Coordinates(p,q );
	                if((m == 0) && (n == 0))
	                	continue;			
	                if ( p<0 || p>=width|| q<0||q>=height)
	                	continue;
	                pix=B.getPixel(p, q);
	                //IJ.log("x = "+p+ "  y = "+q+ "  pix = "+pix );
	                if( pix==0||pix==200)
	                {
	                	neig_count++;
	                	if(pix==0){
	                		NodeList.add(new Coordinates(p,q ));
	                		B.putPixel(p,q, 200);
	                	}
	                	//l.add(new Coordinates(p,q )); 
	                	//ip.putPixel(p, q,255);            	
	                }
	            }
	        }
			
			
			/*int l_size =0;
			int n_neigh =l.size();
			Coordinates C;
			while(!l.isEmpty())
			{
				
				C=l.poll();
				if(BD.getPixel(C.getX(),C.getY())==0)
				{
					BD.putPixel(C.getX(), C.getY(), 2);
					NodeList.add(C);				
				}
			}*/
			//NodeList.addAll(l);
			if(neig_count<8)
			{	
				BoundaryNodes.add(temp);
				IJ.log("B X = "+ temp.getX()+"  Y= "+temp.getY() + " j ="+ j + " pixel value"+ B.getPixel(temp.getX(),temp.getY()));
			}
		}	
		
		//Coordinates
		
		while(!BoundaryNodes.isEmpty())
		{
			temp=BoundaryNodes.poll();
			B.putPixel(temp.getX(), temp.getY(), 0);
			
		}
		
		return BoundaryNodes;
	}

	public void initcolors()
	{
		
		Colors = new int[4];
		int Colors_set [][] = {{255,0,0},{0,255,0},{0,0,255},{150,150,0}};
		BoarderColor =  0<< 16 | 100 << 8 | 200;
		for ( int i =0; i<Colors_set.length;i++)
			Colors[i] = Colors_set[i][0] << 16 | Colors_set[i][1] << 8 | Colors_set[i][2];
	}
	
	public LinkedList<Coordinates> getNeighbours(int x, int y, ByteProcessor ip) {
        LinkedList<Coordinates> l = new LinkedList<Coordinates>();
        int p,q;
        for(int i=-1; i<2; i++) {
        	for(int j=-1; j<2; j++) {
        		p = x+i;
        		q = y + j;
        		//Coordinates temp = new Coordinates(p,q );
                if((i == 0) && (j == 0))
                	continue;			
                if ( p<0 || p>=width|| q<0||q>=height)
                	continue;
                if( ip.getPixel(x+i, y+j)==0)
                {
                	l.add(new Coordinates(p,q )); 
                	ip.putPixel(p, q,255);
                	
                }
            }
        }
        return l;

	}
	
	private static LinkedList<Coordinates> getNeighboursForRG(int x, int y, ByteProcessor ip, ByteProcessor duplicate_ip,LinkedList<Coordinates> FloodFillList) {
        LinkedList<Coordinates> l = new LinkedList<Coordinates>();
        for(int i=-1; i<2; i++) {
                for(int j=-1; j<2; j++) {
                        Coordinates temp = new Coordinates(x+i, y+j);
                        if((i == 0) && (j == 0))
                                continue;
                        else if(set.contains(temp)) {
                                continue;
                        }
						double t = ip.getPixel(x+i, y+j);
                        if( t>=Thresholdlow && t< ThresholdHigh) {
                                //ip.putPixel(temp.getX(), temp.getY(), 255);
                                FloodFillList.add(temp);
                                //CurrentRegions.remove(temp);

                        		l.add(temp);
                                set.add(temp);
                                duplicate_ip.putPixel(temp.getX(), temp.getY(), 255);
                        }
                }
        }

        return l;

}

	
	public static void floodFill(LinkedList<Coordinates> coordlist, ByteProcessor ip, ByteProcessor duplicate_ip, Coordinates temp) {

		LinkedList<Coordinates> list = new LinkedList<Coordinates>();

        set.clear(); // = new HashSet<Coordinates>();


        duplicate_ip.putPixel(temp.getX(), temp.getY(), 255);
        coordlist.add(temp);
        //int p= ip.getPixel(temp.getX(), temp.getY());
        ////IJ.log("p = "+ p);

        list.addAll(getNeighboursForRG(temp.getX(), temp.getY(), ip, duplicate_ip, coordlist));
        //IJ.log("Size of the list = " + list.size());
        while(list.size()!=0) {
                temp = list.poll();
                list.addAll(getNeighboursForRG(temp.getX(), temp.getY(), ip, duplicate_ip, coordlist));
                //IJ.log("Size of the list = " + list.size());

                if (IJ.escapePressed())
                 {

                IJ.log("Aborted");
                break;
                //return null;
            	}
        }

       // IJ.log("processing done");

}

	public LinkedList<Coordinates> ColorBranches( ColorProcessor Prev_orgCp, ColorProcessor orgCp, ColorProcessor cp, ByteProcessor ip, int startx, int starty)
	{
		int bodycolor=3, DoColornumber;
		int numberOfbranch =1;
		LinkedList<Coordinates> branchCoords = new LinkedList<Coordinates>();
		int currentColorN =0;
		WaitCoord temp;
		Coordinates TempCord = null;// = new Coordinates();
		if(globalobjectIP.getPixel(startx,starty)>0)
			DoColornumber=bodycolor;
		else if(Prev_orgCp!=null&&Prev_orgCp.get(startx, starty)>0){
			int c =Prev_orgCp.get(startx, starty);
			int i;
			for( i =0; i<Colors.length;i++)
				if(c==Colors[i])
					break;
			DoColornumber =i;
			currentColorN = DoColornumber;
		}
		else
			DoColornumber = currentColorN;
		
		cp.set(startx,starty,Colors[DoColornumber]);
		branchCoords.add(new Coordinates( startx , starty) );
		orgCp.set(startx,starty,Colors[DoColornumber]);
		//if (starty<5)
		//IJ.log("start pixel =  X = "+ startx + "  y "+ starty);
		ip.putPixel(startx, starty, 255);
		
		LinkedList<Coordinates> l = new LinkedList<Coordinates>();
		l = this.getNeighbours(startx, starty, ip);
		if(l.size()==1)
		{
			TempCord = l.poll();
			FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), currentColorN));
			//IJ.log("Started 1");
		}
		else if(l.size()==2)
		{
			TempCord = l.poll();
			FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), currentColorN));
			TempCord = l.poll();
			FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), currentColorN));	
			//IJ.log("Started 2" + TempCord.getX());
		}
		else if(l.size()==3)
		{
			TempCord = l.poll();
			FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), currentColorN));
			TempCord = l.poll();
			currentColorN =(currentColorN +1)%3;
			numberOfbranch++;
			FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), currentColorN));
			TempCord = l.poll();
			currentColorN =(currentColorN +1)%3;
			numberOfbranch++;
			FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), currentColorN));
			//IJ.log("Started 3");
		}
					
		//FIFOList.add(new WaitCoord(startx,starty, Colors[currentColor]));
		//IJ.log("Fifo size =" + FIFOList.size());
		while(!FIFOList.isEmpty())
		{
			temp = (WaitCoord) FIFOList.removeLast();
			//IJ.log("Fifo size =" + FIFOList.size());
			currentColorN = temp.color;
			
			ByteProcessor BranchIP = (ByteProcessor) ip.duplicate();
			BranchIP.putPixel(temp.cord.getX(), temp.cord.getY(), 255);
			int colortrack[] = new int[5];
			for(int i =0; i<5;i++)
				 colortrack[i]=0;
			l.clear();
			l = this.getNeighbours(temp.cord.getX(), temp.cord.getY(), BranchIP);
			while(l.size()==1)
			{
				TempCord = l.poll();
				int tx = TempCord.getX(), ty =TempCord.getY();
				BranchIP.putPixel(tx,ty, 255);
				if(globalobjectIP.getPixel(tx,ty)>0)
					colortrack[3]=colortrack[3]+1;
				else if(Prev_orgCp!=null&&Prev_orgCp.get(tx,ty)>0){
					int c = Prev_orgCp.get(tx,ty);
					int i;
					for( i =0; i<Colors.length;i++)
						if(c==Colors[i])
							break;
					colortrack[i]=colortrack[i]+1;
				}
				else
					colortrack[4]=colortrack[4]+1;
				l.clear();
				l = this.getNeighbours(tx, ty, BranchIP);
				//IJ.log("list size  In loop=" + l.size());
			}
			
			int max = colortrack[0];
			int index=0;
			for(int i =0; i<5;i++)
				if(max<colortrack[i]){
					max=colortrack[i];
					index =i;
				}
			if(index==4){
				currentColorN = (currentColorN+1)%3;
			}
			else 
				currentColorN =index;
			DoColornumber=currentColorN;
			
			
			ip.putPixel(temp.cord.getX(), temp.cord.getY(), 255);
			/*if(globalobjectIP.getPixel(temp.cord.getX(), temp.cord.getY())>0)
				DoColornumber=bodycolor;
			else if(Prev_orgCp!=null&&Prev_orgCp.get(temp.cord.getX(), temp.cord.getY())>0){
				int c =Prev_orgCp.get(temp.cord.getX(), temp.cord.getY());
				int i;
				for( i =0; i<Colors.length;i++)
					if(c==Colors[i])
						break;
				DoColornumber =i;
				currentColorN = DoColornumber;
			}
			else
				DoColornumber = currentColorN;*/
			cp.set(temp.cord.getX(), temp.cord.getY(), Colors[DoColornumber]);
			branchCoords.add(temp.cord);
			orgCp.set(temp.cord.getX(), temp.cord.getY(), Colors[DoColornumber]);
			l.clear();
			l = this.getNeighbours(temp.cord.getX(), temp.cord.getY(), ip);
			//IJ.log("list size =" + l.size());
			int branchlength  =1;
			while(l.size()==1)
			{  branchlength++;
				TempCord = l.poll();
				int tx = TempCord.getX(), ty =TempCord.getY();
				ip.putPixel(tx,ty, 255);
				if(globalobjectIP.getPixel(tx,ty)>0)
					DoColornumber=bodycolor;
				/*else if(Prev_orgCp!=null&&Prev_orgCp.get(tx,ty)>0){
					int c = Prev_orgCp.get(tx,ty);
					int i;
					for( i =0; i<Colors.length;i++)
						if(c==Colors[i])
							break;
					DoColornumber =i;
					currentColorN = DoColornumber;
				}*/
				else
					DoColornumber = currentColorN;
				
				cp.set(tx, ty, Colors[DoColornumber]);
				branchCoords.add(TempCord);
				orgCp.set(tx, ty, Colors[DoColornumber]);
				l.clear();
				l = this.getNeighbours(tx, ty, ip);
				//IJ.log("list size  In loop=" + l.size());
			}
			//IJ.log(" Length of the baracn starting with = ");
			if(l.size()==2)
			{
				Coordinates C1 = l.poll();
				Coordinates C2 = l.poll();
				/* checks if two new neighbours are adjacent, then*/
				if( ( (C1.getX()==C2.getX()) && (Math.abs(C1.getY()-C2.getY()) == 1) ) ||
						( (C1.getY()==C2.getY()) && (Math.abs(C1.getX()-C2.getX()) == 1) )){
					//IJ.log("Yes  X ="+TempCord.getX()+" Y = "+TempCord.getY());
					if(C1.getX()==TempCord.getX()|| C1.getY()==TempCord.getY()){
						TempCord=C1;
						ip.putPixel(C2.getX(), C2.getY(), 0);
					}	
					else{
						TempCord=C2;
						ip.putPixel(C1.getX(), C1.getY(), 0);
					}
					
						
					FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), currentColorN));
				}
				else{
					currentColorN =(currentColorN +1)%3;
					numberOfbranch++;
					//TempCord = l.poll();
					FIFOList.add(new WaitCoord(C1.getX(),C1.getY(), currentColorN));
					currentColorN =(currentColorN +1)%3;
					numberOfbranch++;
					//TempCord = l.poll();
					FIFOList.add(new WaitCoord(C2.getX(),C2.getY(), currentColorN));	
				}	
			}
			if(l.size()==3)
			{
				int pcolor = currentColorN;
				int pCordx = TempCord.getX();
				int pCordy = TempCord.getY();
				int c;
				TempCord = l.poll();
				if(pCordx==TempCord.getX()||pCordy==TempCord.getY())
					c = pcolor;
				else {
					currentColorN= (currentColorN+1)%3;
					c = currentColorN;
					numberOfbranch++;
				}									
				FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), c));
				
				TempCord = l.poll();
				if(pCordx==TempCord.getX()||pCordy==TempCord.getY())
					c = pcolor;
				else {
					currentColorN= (currentColorN+1)%3;
					c = currentColorN;
					numberOfbranch++;
				}
				FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), c));
				
				TempCord = l.poll();
				if(pCordx==TempCord.getX()||pCordy==TempCord.getY())
					c = pcolor;
				else {
					currentColorN= (currentColorN+1)%3;
					c = currentColorN;
					numberOfbranch++;
				}
				FIFOList.add(new WaitCoord(TempCord.getX(),TempCord.getY(), c));
				//IJ.log("Started 3");
			}
		}
		//IJ.log("number of branches = "+numberOfbranch);
		return branchCoords;
		
	}
	
	private void ColorGrowing(ImagePlus imorg, ColorProcessor orgCp, ImageProcessor ip,LinkedList<Coordinates> branchCoords) throws InterruptedException {
		// TODO Auto-generated method stub
		LinkedList<Coordinates> neighbours = new LinkedList<Coordinates>();
		
		Coordinates temp;
		int pixelColor;
		ByteProcessor dupip = (ByteProcessor) ip.duplicate();		
		dupip.invert();
		Coordinates El;
		int k=branchCoords.size()-1;
		while(k>=0){
			El = branchCoords.get(k);
			dupip.putPixel(El.getX(), El.getY(), 255);
			k--;
		}
		//ImagePlus dupimg = new ImagePlus("Duptlitate IP",orgCp);
		k=0;
		//totalVoxel = totalVoxel + branchCoords.size();
		while(!branchCoords.isEmpty())
		{
			totalVoxel++;
			temp = branchCoords.poll();
			pixelColor=orgCp.get(temp.getX(), temp.getY());
			//IJ.log("Color" + pixelColor+ " Red "+ Colors[0]);
			dupip.putPixel(temp.getX(), temp.getY(),255);
			neighbours= this.getNeighbours(temp.getX(), temp.getY(), dupip);
			
			//if(neighbours.size()<3)//boundary pixel
				//orgCp.set(temp.getX(), temp.getY(), BoarderColor);
				
			while(!neighbours.isEmpty())
			{
				temp = neighbours.poll();
				if(temp.getX()!=width&& temp.getY()!=height)
				{
					//IJ.log("Cordx= "+ temp.getX()+ "  CordY "+temp.getY());
					orgCp.set(temp.getX(), temp.getY(), pixelColor);
					imorg.show();
					imorg.updateAndDraw();
					//Thread.sleep(1);
					branchCoords.add(temp);
				}
			}			
		}
		//dupimg.close();
		//IJ.log("one branch done");
		
	}
	///////////////////////
	public void VolumeSurface(ImageProcessor ip) {
		// TODO Auto-generated method stub
		height = ip.getHeight();
		width = ip.getWidth();
		//int [][]Values = new int[width][height];
		ImageStack stack = imp.getStack();
		ImageStack binarystack = imp.createEmptyStack();
		int st =1;
		int i,j,found=0;
		int totalvol=0,volu=0,totalarea=0,totalSides=0,totaltb=0,surareaS=0,surareatb=0;
		int stacksize = stack.getSize();
		while(st<=stacksize){
			volu=0;
			surareaS=0;
			surareatb=0;
			ByteProcessor B1 = null,B0 = null;
			ByteProcessor B = (ByteProcessor)stack.getProcessor(st).duplicate();
			if((st+1)<=stacksize){
			 B1 = (ByteProcessor)stack.getProcessor(st+1).duplicate();
			}
			if((st-1)>=1){
			 B0 = (ByteProcessor)stack.getProcessor(st-1).duplicate();
			}
			
			for ( j =0; j< B.getHeight(); j++)
				for( i  =0; i<B.getWidth(); i++)
				{
					//IJ.log
				    if (B.getPixel(i, j)!=0){
				    	volu++;
				  }
				    if(st==1){if (B.getPixel(i, j)!=0){
				    	
				    	 if ((B.getPixel(i-1, j)==0)){
			    			 surareaS++;
			    			 
			    		 }
			    		 if((B.getPixel(i+1, j)==0)){
			    			 surareaS++;
			    		 }
			    		 if((B.getPixel(i, j-1)==0)){
			    			 surareaS++;	 
			    		 }
			    		 if((B.getPixel(i, j+1)==0)){
			    			 surareaS++;	 
			    		 }
			    		 if (B.getPixel(i, j)!=0){
				    		 surareatb++;
			    		 } 
				    }}
				    else if(st==stacksize){
				    	 if (B.getPixel(i, j)!=0){
				    		 surareatb++;
				    		 
				    		 if ((B.getPixel(i-1, j)==0)){
				    			 surareaS++;
				    			 
				    		 }
				    		 if((B.getPixel(i+1, j)==0)){
				    			 surareaS++;
				    		 }
				    		 if((B.getPixel(i, j-1)==0)){
				    			 surareaS++;	 
				    		 }
				    		 if((B.getPixel(i, j+1)==0)){
				    			 surareaS++;	 
				    		 }
				    		 
						  }
				    }
				    else{
				    	 if (B.getPixel(i, j)!=0){
				    		 if ((B.getPixel(i-1, j)==0)){
				    			 surareaS++;
				    			 
				    		 }
				    		 if((B.getPixel(i+1, j)==0)){
				    			 surareaS++;
				    		 }
				    		 if((B.getPixel(i, j-1)==0)){
				    			 surareaS++;	 
				    		 }
				    		 if((B.getPixel(i, j+1)==0)){
				    			 surareaS++;	 
				    		 }
				    		 if((B1.getPixel(i, j)==0)){
				    			 surareatb++;
				    		 }
				    		 if((B0.getPixel(i, j)==0)){
				    			 surareatb++;	 
				    		 }
				    		 
				    		 
				    	 }
				    	
				    	
				    	
				    	
				    }
				    
				    
				    
				    
				    }
			//IJ.log("Volu in  :"+st+" : "+volu+" area Sides:" + surareaS+" : area tob and bottom :"+surareatb);
			st++;
			binarystack.addSlice(B);
totalvol=totalvol+volu;
totalSides=totalSides+surareaS;
totaltb=totaltb+surareatb;
		}
		IJ.log("Volume : "+totalvol+" ,  No of area sides  : " +totalSides+" ,Total Top_Bottoms "+totaltb);
		Whole_RT.reset();
		Whole_RT.incrementCounter();
		Whole_RT.addValue("Volume", totalvol);
		Whole_RT.addValue("Side Pixels", totalSides);
		Whole_RT.addValue("Top Pixels", totaltb);
		Whole_RT.addValue("Surface Area(pixels) ", totaltb+totalSides);
		Whole_RT.show("Whole Object Data");


	}
	////////////////////////
	
	
	public void CalculateLengthWidth(ImageProcessor ip, int s) {
		Random rand = new Random();
		// TODO Auto-generated method stub
		height = ip.getHeight();
		width = ip.getWidth();
		Map<Integer,Integer[]> pixels = new HashMap<Integer,Integer[]>();
//Integer Xp[]=new Integer[width];
//Integer Yp[]=new Integer[height];

		ByteProcessor sk=(ByteProcessor)ip.duplicate();
		sk.invert();
		sk.skeletonize();
		sk.invert();
		
		ImagePlus ipsk=new ImagePlus("skeleton",sk);
		ipsk.show();
		ByteProcessor ou=(ByteProcessor)ip.duplicate();
		ou.findEdges();
		ImagePlus ipou=new ImagePlus("edged",ou);
		ipou.show();
		AnalyzeSkeleton_ as1=new AnalyzeSkeleton_();
		//as1.processSkeleton(sk);
		//as1.run(sk);
		
		as1.setup("test", new ImagePlus("test",sk));
		//as1.run();
		as1.run(0, false, true, new ImagePlus("test",sk), false, false);
		@SuppressWarnings("static-access")
		ArrayList< Double > sp;
		 sp =  as1.shortestPathList;
		 length_all[s]=sp.get(0);
	   
		 //ImageStack stt;
		 //stt=as1.shortPathImage;
		 ImagePlus stimp =  as1.returnImageLong();//new ImagePlus("obtained",stt);
		 stimp.show();
		 
		 ImageProcessor rp =stimp.getProcessor();
		 ByteProcessor skr=(ByteProcessor)ip.createProcessor(ip.getWidth()	, ip.getHeight());
		 
		 for(int a=0;a<stimp.getWidth();a++){
			 for(int b=0;b<stimp.getHeight();b++){
				 if(rp.getPixel(a, b)==96)
					 skr.putPixel(a, b, 255);
			 }
		 }
		 
		 
		 ImagePlus finalskl = new ImagePlus("final skel", skr);
		 finalskl.show();
		 sk =skr;
		
		IJ.log("length "+sp.get(0));
		
		
		
int count=0;
		for(int i=0;i<width;i++){
			for(int j=0;j<height;j++){
				Integer pixel[]=new Integer[2];
				if (ou.getPixel(i, j)==255){
					pixel[0]=i;
					pixel[1]=j;
					pixels.put(count, pixel);
					count++;
				}
				
				
				
				
			}

		}
		
		int y=pixels.size();
		//int tn =;
		int nos=6;
		
		if(y<6)
			y =1;
			
		

		Integer randomnum[] =new Integer[nos];
		Integer flag[] =new Integer[nos];

		for(int m=0;m<nos;m++)
			randomnum[m]= rand.nextInt((y - 1) + 1) + 1;
		
		
		/*Integer fsd[]=new Integer[2];
		for(int l=0;l<y;l++){
			

			fsd=pixels.get(l);
			System.out.println(fsd[0]+"y="+fsd[1]);
			
		}*/
		int flagsum=0;
		for(int z=0;z<nos;z++){
	ByteProcessor	sk1=(ByteProcessor) sk.duplicate();
		
		Integer verify[]=new Integer[2];
		verify=pixels.get(randomnum[z]);
		int counter=1;
		while(sk1.getPixel(verify[0], verify[1])!=255){
			sk1.erode();
			flag[z]=counter;
		counter++;
		}
		flagsum=flagsum+flag[z];
		System.out.println("pixels are "+verify[0]+" y "+verify[1]+" width "+flag[z]);

		}
		System.out.println("Final average width is : "+((flagsum/nos)*2)+":::"+nos);
width_all[s]=((flagsum/nos)*2);
		}
	

	@Override
	public int setup(String arg0, ImagePlus im) {
		// TODO Auto-generated method stub
		this.imp = im;
		Title = imp.getTitle().toString();
		return DOES_8G;
	}
}