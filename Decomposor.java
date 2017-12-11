/**
 * @author Ciprian (Andy) Triculescu
 * CS310
 * Fall 2017
 */

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.LinkedList;
import java.util.Comparator;
import java.util.Collections;
import java.util.AbstractCollection;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.Color;

import javax.swing.JPanel;

/**
 * This class uses a disjoint set of pixels to reduce the amount of colors in an image to a specified amount. 
 */
public class Decomposor extends JPanel
{
    /**
     * Gets the integer ids of the roots of the neighbor sets of pixel with id root.
     * These regions are all adjacent around the location of pixel with id root.
     * @param ds The disjoint set
     * @param root The root to get neighbor sets around
     * @return A Treeset with the roots of all the neighboring regions different than this one.  
     */
    private TreeSet<Integer> getNeightborSets(DisjointSets<Pixel> ds, int root)
    {
      TreeSet<Integer> outputRootID = new TreeSet<Integer>();
      Pixel rootpix = getPixel(root);
      ArrayList<Pixel> rootneighbors = getNeightbors(rootpix);
      for(Pixel x: rootneighbors){
        int temp = ds.find(getID(x));
        if(temp!=root)
          outputRootID.add(temp);
      }
      return outputRootID;
    }
    /**
     * This method returns a similarity between the pixels at root1 and root2 in the disjoint set ds. 
     * Uses a pre-determined algorithm to get the average color of the two pixels, summing the total difference in the sets. 
     * @param ds The disjoint set. 
     * @param root1 The first root to compare
     * @param root2 The second root to compare
     * @return The similarity between the two roots. 
     */
    private Similarity getSimilarity(DisjointSets<Pixel> ds, int root1, int root2)
    {
      Pixel pixelr1 = getPixel(root1);
      Pixel pixelr2 = getPixel(root2);
      Set<Pixel> r1set = ds.get(root1);
      Set<Pixel> r2set = ds.get(root2);
      Color avgr1 = computeAverageColor(r1set);
      Color avgr2 = computeAverageColor(r2set);
      int cred = ((avgr1.getRed() * r1set.size())+(avgr2.getRed() * r2set.size()))/(r1set.size() + r2set.size());
      int cgreen = ((avgr1.getGreen() * r1set.size())+(avgr2.getGreen() * r2set.size()))/(r1set.size() + r2set.size());
      int cblue = ((avgr1.getBlue() * r1set.size())+(avgr2.getBlue() * r2set.size()))/(r1set.size() + r2set.size());
      Color c = new Color(cred, cgreen, cblue);
      int sumdiff = 0;
      for(Pixel x:r1set){
        sumdiff+=getDifference(getColor(x), c);
      }
      for(Pixel x:r2set){
        sumdiff += getDifference(getColor(x), c);
      }
      return new Similarity(sumdiff, pixelr1, pixelr2); 
    }
    /**
     * Segment is the meat of the decomposer and the project due to everything culminating inside of it. 
     * First, the disjoint set ds is created from the data extracted from the image. Then the priorityqueue of similarities is filled using ds. 
     * Then, the program iterates until only K regions are left in the disjoint set ds, printing progress along the way. 
     * Each similarity is then extracted from the priority queue, with the most similar regions being removed first. If p and q are not part of the same region, 
     * They are further analyzed. If either p or q is not the root of its own set, and if the difference between p and q is zero (very similar), then they are unioned.
     * If the distance between p and q here is nonzero, they are added back to the queue. Otherwise, if both p and q are roots of their sets, we only operate on them
     * If their similarity is the same as the initial similarity they claimed to have. We then union these p and q roots, and if they are not very similar we also add the pixels 
     * in the region. 
     * @param K The number of regions to reduce the disjoint set of the image to. 
     */
    public void segment(int K) //K is the number of desired segments
    {
      if(K<2)
          throw new IllegalArgumentException(new String("! Error: K should be greater than 1, current K="+K));
      int width = this.image.getWidth();
      int height = this.image.getHeight();
      ArrayList<Pixel> datainput = new ArrayList<Pixel>(width*height);
      for(int h=0; h<height; h++){
        for(int w=0; w<width;w++)
          datainput.add(new Pixel(w,h));
      }
      ds = new DisjointSets<Pixel>(datainput);
      PriorityQueue<Similarity> pq = new PriorityQueue<Similarity>();
      for(int i = 0; i<datainput.size(); i++){
        ArrayList<Pixel> neighbors = getNeightbors(datainput.get(i));
        for(Pixel j:neighbors)
          pq.add(getSimilarity(ds,i,getID(j)));
      }
      int iter = 0;//number of iterations to update on progress
      while(ds.getNumSets()>K){
        if(iter == 500){
          tell_progress(K);
          iter = 0;//reset the iterations
        }
        iter++;
        Similarity temp = pq.remove();//get the most similar pair from the pq
        int p = getID(temp.pixels.p);
        int q = getID(temp.pixels.q);
        int proot = ds.find(p);//roots of pixel p and q
        int qroot = ds.find(q);
        int dist = temp.distance; //distance between pixels p and q
        if(proot!=qroot){
          if((p!=proot) || (q!=qroot)){
            if(dist==0)
              ds.union(proot,qroot);//union at the root only
            else if(dist > 0)
              pq.add(getSimilarity(ds,proot,qroot));
          }
          else if((p==proot)&&(q==qroot)){
            Similarity temp2 = getSimilarity(ds,p,q);
            if(temp2.distance == dist){
              int nr = ds.union(p,q);//since p and q are both roots, safe to union p and q here
              if(dist != 0){
                Set<Pixel> regionr = ds.get(nr);
                for(Pixel pp:regionr){//iterate through the region to add similarity between those pixels and the new unioned root.
                TreeSet<Integer> nneighbors = getNeightborSets(ds,getID(pp));
                for(int nneighbor:nneighbors){
                  pq.add(getSimilarity(ds,nr,nneighbor));
                }
              }
              }
            }
          }
        }
      }
    }

    /** 
     * Coded by professor lien, prints progress to the screen while segment is running. 
     * @param K The same K that segment has, aka the regions to reduce to. 
     */
    private void tell_progress(int K) //K is the same as in segment(int K)
    {
      float progress = (100.0f*K)/ds.getNumSets();
      int p = (int)Math.floor(progress);
      String bar="["+(new String(new char[p]).replace("\0","*")) +(new String(new char[100-p]).replace("\0","-"))+"]";
      System.err.print("Progress: "+String.format("%.02f",progress)+"% "+bar+"\r");
    }
    /**
     * Outputs the results of what segment provided, and recolors the pixels iteratively
     * This method also outputs the image statistics to the command line as its recoloring. 
     * @param K The K regions remaining. 
     */
    public void outputResults(int K)
    {
        //System.out.println("Here!");
        //collect all sets
        int region_counter=1;
        ArrayList<Pair<Integer>> sorted_regions = new ArrayList<Pair<Integer>>();

        int width = this.image.getWidth();
        int height = this.image.getHeight();
        for(int h=0; h<height; h++){
          for(int w=0; w<width; w++){
              int id=getID(new Pixel(w,h));
              int setid=ds.find(id);
              if(id!=setid) continue;
              sorted_regions.add(new Pair<Integer>(ds.get(setid).size(),setid));
          }//end for w
        }//end for h

        //sort the regions
        Collections.sort(sorted_regions, new Comparator<Pair<Integer>>(){
          @Override
          public int compare(Pair<Integer> a, Pair<Integer> b) {
              if(a.p!=b.p) return b.p-a.p;
              else return b.q-a.q;
          }
        });
        //iterate through the sorted regions
      for(int i=0; i<sorted_regions.size(); i++){
        Pair<Integer> temp = sorted_regions.get(i);
        int setsize = temp.p;
        int setroot = temp.q;
        //Get a temporary set from the root in the pair from sorted regions
        Set<Pixel> tempset = ds.get(setroot);
        Color c = computeAverageColor(tempset);
        System.out.println("region " + (i+1) + " size= " + tempset.size() + " color=" + c.toString()); //Print image region statistics. 
        for(Pixel p: tempset){
          image.setRGB(p.p, p.q, c.getRGB());
        }
      }

      String out_filename = img_filename+"_seg_"+K+".png";
      try
      {
        File ouptut = new File(out_filename);
        ImageIO.write(this.image, "png", ouptut);
        System.err.println("- Saved result to "+out_filename);
      }
      catch (Exception e) {
        System.err.println("! Error: Failed to save image to "+out_filename);
      }
    }

    //-----------------------------------------------------------------------
    //
    //
    // Todo: Read and provide comments, but do not change the following code
    //
    //
    //-----------------------------------------------------------------------
    /**
     * The image to decompose, represented as a 2D array of RGB pixels. 
     */
    public BufferedImage image;       //this is the 2D array of RGB pixels
    /** 
     * The filename of the image being inputted
     */
    private String img_filename;      //input image filename without .jpg or .png
    /** 
     * The disjoint set of all the pixels in the image.
     */
    private DisjointSets<Pixel> ds;   //the disjoint set

    //
    // constructor, read image from file
    //
    /**
     * Constructor, used to read the image file specified in
     * @param imgfile The location of the image
     */
    public Decomposor(String imgfile)
    {
      File imageFile = new File(imgfile);
      try
      {
        this.image = ImageIO.read(imageFile);
      }
      catch(IOException e)
      {
        System.err.println("! Error: Failed to read "+imgfile+", error msg: "+e);
        return;
      }
      this.img_filename=imgfile.substring(0, imgfile.lastIndexOf('.')); //remember the filename
    }


    //
    // 3 private classes below
    //
    /**
     * Private class representing a pair of two objects.
     */
    private class Pair<T>
    {
      public Pair(T p_, T q_){this.p=p_;this.q=q_;}
      T p, q;
    }

    //a pixel is a 2D coordinate (w,h) in an image
    /** 
     * Representation of a pixel, or a coordinate set within an image
     */
    private class Pixel extends Pair<Integer>{ public Pixel(int w, int h){ super(w,h); } } //aliasing Pixel

    //this class represents the similarity between the colors of two adjacent pixels or regions
    /** 
     * A class representing the distance, or similarity between two adjacent pixels to be used in the priority queue to determine which pixels are most similar. 
     */
    private class Similarity implements Comparable<Similarity>
    {
      /** 
       * A constructor for a similarity, includes the difference and two pixels
       * @param d The difference or distance between the colors of the two pixels
       * @param p The first pixel referenced
       * @param q The second pixel referenced
       */
      public Similarity(int d, Pixel p, Pixel q)
      {
        this.distance=d;
        this.pixels=new Pair<Pixel>(p,q);
      }
      /** 
       * Compares two similarities between their distances of their two pixels
       * @param other The other similarity to compare to
       * @return The difference between the similarities. 
       */
      public int compareTo( Similarity other )
      {
        //remove ambiguity~ update: 11/28/2017
        int diff=this.distance - other.distance;
        if(diff!=0) return diff;
        diff=getID(this.pixels.p) - getID(other.pixels.p);
        if(diff!=0) return diff;
        return getID(this.pixels.q) - getID(other.pixels.q);
      }

      //a pair of ajacent pixels or regions (represented by the "root" pixels)
      /** 
       * The pair of adjacent pixels or regions we have the difference of
       */
      public Pair<Pixel> pixels;

      //distance between the color of two pixels or two regions,
      //smaller distance indicates higher similarity
      /** 
       * The distance or difference between the two pixels. 
       */
      public int distance;
    }

    //
    // helper functions
    //

    //convert a pixel to an ID
    /** 
     * Returns the ID of the parameter pixel
     * @param pixel The pixel to get the ID of
     * @return The ID of the pixel. 
     */
    private int getID(Pixel pixel)
    {
      return this.image.getWidth()*pixel.q+pixel.p;
    }

    //convert ID back to pixel
    /** 
     * Gets the pixel situated at the ID parameter
     * @param id The ID to find the pixel at
     * @return The pixel at the ID
     */
    private Pixel getPixel(int id)
    {
      int h= id/this.image.getWidth();
      int w= id-this.image.getWidth()*h;

      if(h<0 || h>=this.image.getHeight() || w<0 || w>=this.image.getWidth())
        throw new ArrayIndexOutOfBoundsException();

      return new Pixel(w,h);
    }
	/** 
   * Returns the RGB color of the pixel p of the image
   * @param p The pixel to get the color of
   * @return The color of pixel p
   */
	private Color getColor(Pixel p) {
		return new Color(image.getRGB(p.p, p.q));
	}

    //compute the average color pf a collection of pixels
    /** 
     * Returns the average color of a collection (set) of pixels
     * @param pixels The set of pixels to get the avg color of
     * @return The average color
     */
    private Color computeAverageColor(AbstractCollection<Pixel> pixels)
    {
      int r=0, g=0, b=0;
      for(Pixel p : pixels)
      {
        Color c = new Color(image.getRGB(p.p, p.q));
        r+=c.getRed();
        g+=c.getGreen();
        b+=c.getBlue();
      }
      return new Color(r/pixels.size(),g/pixels.size(),b/pixels.size());
    }
    /** 
     * Gets the difference in color between two colors
     * @param c1 The first color
     * @param c2 The second color
     * @return The difference in color
     */
    private int getDifference(Color c1, Color c2)
    {
      int r = (int)(c1.getRed()-c2.getRed());
      int g = (int)(c1.getGreen()-c2.getGreen());
      int b = (int)(c1.getBlue()-c2.getBlue());

      return r*r+g*g+b*b;
    }

    /** 
     * Returns the 8 neighboring pixels of one pixel in an arraylist
     * @param pixel The pixel we're getting the neighbors of
     * @return The arraylist of 8 pixels around Pixel pixel
     */
    private ArrayList<Pixel> getNeightbors(Pixel pixel)
    {
      ArrayList<Pixel> neighbors = new ArrayList<Pixel>();

      for(int i=-1;i<=1;i++)
      {
        int n_w=pixel.p+i;
        if(n_w<0 || n_w==this.image.getWidth()) continue;
        for(int j=-1;j<=1;j++)
        {
          int n_h=pixel.q+j;
          if(n_h<0 || n_h==this.image.getHeight()) continue;
          if(i==0 && j==0) continue;
          neighbors.add( new Pixel(n_w, n_h) );
        }//end for j
      }//end for i

      return neighbors;
    }

    /** 
     * Paints the image after recoloring
     * @param g Graphics parameter to paint the image with
     */
    public void paint(Graphics g)
    {
      //12f8388e73ae05b92056865f9170525e
      g.drawImage(this.image, 0, 0,this);
    }
}

