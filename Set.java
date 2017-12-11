/**
 * @author Ciprian (Andy) Triculescu
 * CS310
 * Fall 2017
 */

//You cannot import additonal items
import java.util.AbstractCollection;
import java.util.Iterator;
//You cannot import additonal items

/**
 * Simple implementation of a set that uses a dynamic array data structure backend. 
 */

public class Set<T> extends AbstractCollection<T>
{
  /**
   * The current size or number of elements in the set. Starts out at zero.
   */
  private int size = 0;
  /**
   * The data of this set, starts out with size 100
   */
  private Object tset[] = new Object[100];
  
  private void resize(){
    
    Object copyset[] = new Object[tset.length];
    for(int i=0; i<tset.length; i++){
      copyset[i] = tset[i];
    }

    Object emptyset[] = new Object[copyset.length*2];
    tset = emptyset;
    for(int j=0; j<copyset.length; j++){
      tset[j] = copyset[j];
    }
  }
  /**
   * Adds one t item to the array, resizes if necessary if at 80% capacity. 
   * Increments size once added, returns true. 
   * @param item The item to add. 
   * @return True in all cases. 
   */
  public boolean add(T item)
  {
    if(size() >= (int)(0.8*(tset.length))){
      resize();
    }
    tset[size] = item;
    size++;
    return true;
  }
  
  /** 
   * Adds all the elements of set other to this set. Uses an iterator to iterate through other, and uses this' add method at each element.
   * @param other The set to copy to the end of this set.
   * @return True in all cases. 
   */
  public boolean addAll(Set<T> other)
  {
    Iterator<T> iter = other.iterator();
    while(iter.hasNext())
      this.add(iter.next());
    return true;
  }
  
  /**
   * Clears out the data and size for a fresh new set
   */
  public void clear()
  {
    int oldlength = tset.length;
    size = 0;
    tset = new Object[oldlength];
  }
  
  /**
   * Returns the size or number of elements currently in the set
   * @return the size
   */
  public int size()
  {
    return size;
  }
  /**
   * An iterator over this set, checks to see if an object has next and gives the next element, incrementing by one each time.
   * @return The iterator
   */
  public Iterator<T> iterator()
  {
    return new Iterator<T>()
    {
      /**
       * The current position inside the dynamic data array, starts at zero.
       */
      private int pos = 0;

      /**
       * Returns the next element in the array, assuming there is one. Increments pos.
       * @return the next (actually current) element
       */
      @SuppressWarnings("unchecked")public T next()
      {
        T nextt = (T)tset[pos];
        pos++;
        return nextt;
      }
      /**
       * Checks to see if there is more data to be read in the array.
       * If pos is currently at the size of the array, it means this index will be a null
       * @return true if there will be more data
       */
      public boolean hasNext()
      {
        return(pos != size());
      }
    };
  }
}
