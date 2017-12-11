/**
 * @author Ciprian (Andy) Triculescu
 * CS310
 * Fall 2017
 */

import java.util.ArrayList;

/**
 * Disjoint sets class, using union by size and path compression. Holds an arraylist of sets.
 */
public class DisjointSets<T>
{
    /** 
     * The array holding the sizes (negative) and pointers to unioned off locations
     */
    private int [ ] s;
    /** 
     * The arraylist of sets holding the data
     */
    private ArrayList<Set<T>> sets;
    /** 
     * The size of the sets, or amount of non-unioned sets remaining, need this because we're adding nulls into Alist sets
     */
    private int setsSize;
    /**
     * Constructor for DisjointsSets. Takes in parameter data, and reads that data making disjoint sets from each data point.
     * Sets up s afterwards as well. 
     * @param data The data to use in our disjoint sets. 
     */
    public DisjointSets( ArrayList<T> data  )
    {
      sets = new ArrayList<Set<T>>(data.size());
      for(int i=0; i<data.size();i++){
        Set<T> temp = new Set<T>();
        temp.add(data.get(i));
        if(!sets.contains(temp)){
          sets.add(temp);
          setsSize++;
        }
      } 
      s = new int [sets.size()];
      for(int i =0; i<sets.size(); i++)
        s[i] = -1;
    }

    /**
     * Union algorithm for root1, root2. Returns the index of where we choose the root to be between these two. 
     * Algorithm taken from textbook, all we add to it is removing from the sets arraylist to simulate the data being connected to the new root. 
     * @param root1 The first root
     * @param root2 The second root
     * @throws IllegalArgumentException if either root is out of bounds, if either of which is not a root, or if they are equal. Can't union something to itself. 
     * @return The new root chosen. 
     */
    public int union( int root1, int root2 )
    {
      if((root1<0)||(root1>=s.length)||(root2<0)||(root2>=s.length)||(s[root1]>=0)||(s[root2]>=0)||(root1==root2))//for out of bounds or illegal roots
        throw new IllegalArgumentException();
      if(s[root2]<s[root1]){
        Set<T> temp = sets.set(root1, null);
        setsSize--;
        sets.get(root2).addAll(temp);
        s[root2] += s[root1];
        s[root1] = root2;
        return root2;
      }
      Set<T> temp = sets.set(root2, null);
      setsSize--;
      sets.get(root1).addAll(temp);
      s[root1]+=s[root2];
      s[root2] = root1;
      return root1;
    }

    /**
     * Find algorithm to find where int x is rooted at. If it is negative it is a root so you return that index, otherwise recursively iterate through the sets to finally return when the root is reached.
     * @param x The index to start searching at or to recursively iterate through. 
     * @return Where the initial x is rooted at. 
     */
    public int find( int x )
    {
      if((x<0)||(x>=s.length))
        throw new IllegalArgumentException();
      int pos = x;
      while(s[pos] >= 0){
        pos = s[pos];
      }
      return pos;
    }

    /**
     * Gets the root set of the argument root passed in. 
     * @param root The root to find and get the root set for. 
     * @return The set root is rooted in/at.
     */
    public Set<T> get( int root )
    {
      if((root<0)||(root>=s.length))
        throw new IllegalArgumentException();
      return sets.get(root);
    }

    /**
     * Gets the length of arraylist sets, which cooresponds to the number of sets remaining that have not been unioned off. Will eventually be one after all are unioned. 
     * @return The number of sets (roots) remaining. 
     */
    public int getNumSets()
    {
      return setsSize; 
    }
}