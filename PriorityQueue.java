/**
 * @author Ciprian (Andy) Triculescu
 * CS310
 * Fall 2017
 */
//
// This is from your textbook, no need to add or modify anything here
//


import java.util.NoSuchElementException;
import java.util.Iterator;

/**
 * PriorityQueue class implemented via the binary heap.
 */
public class PriorityQueue<T extends Comparable<T>>
{
    /**
     * Construct an empty PriorityQueue.
     */
    @SuppressWarnings("unchecked")
    public PriorityQueue()
    {
        currentSize = 0;
        array = (T[]) new Comparable[ DEFAULT_CAPACITY + 1 ];
    }

    /**
     * Compares lhs and rhs using comparator if
     * provided by cmp, or the default comparator.
     */
    private int compare( T lhs, T rhs )
    {
        return lhs.compareTo(rhs);
    }

    /**
     * Adds an item to this PriorityQueue.
     * @param x any object.
     * @return true.
     */
    public boolean add( T x )
    {
        if( currentSize + 1 == array.length )
            doubleArray( );

            // Percolate up
        int hole = ++currentSize;
        array[ 0 ] = x;

        for( ; compare( x, array[ hole / 2 ] ) < 0; hole /= 2 )
            array[ hole ] = array[ hole / 2 ];
        array[ hole ] = x;

        return true;
    }

    /**
     * Returns the number of items in this PriorityQueue.
     * @return the number of items in this PriorityQueue.
     */
    public int size( )
    {
        return currentSize;
    }

    /**
     * Make this PriorityQueue empty.
     */
    public void clear( )
    {
        currentSize = 0;
    }

    /**
     * Returns an iterator over the elements in this PriorityQueue.
     * The iterator does not view the elements in any particular order.
     */
    public Iterator<T> iterator( )
    {
        return new Iterator<T>( )
        {
            int current = 0;

            public boolean hasNext( )
            {
                return current != size( );
            }

            public T next( )
            {
                if( hasNext( ) )
                    return array[ ++current ];
                else
                    throw new NoSuchElementException( );
            }

            public void remove( )
            {
                throw new UnsupportedOperationException( );
            }
        };
    }

    public boolean isEmpty( )
    {
      return currentSize==0;
    }

    /**
     * Returns the smallest item in the priority queue.
     * @return the smallest item.
     * @throws NoSuchElementException if empty.
     */
    public T root( )
    {
        if( isEmpty( ) )
            throw new NoSuchElementException( );
        return array[ 1 ];
    }

    /**
     * Removes the smallest item in the priority queue.
     * @return the smallest item.
     * @throws NoSuchElementException if empty.
     */
    public T remove( )
    {
        T minItem = root( );
        array[ 1 ] = array[ currentSize-- ];
        percolateDown( 1 );

        return minItem;
    }


    /**
     * Establish heap order property from an arbitrary
     * arrangement of items. Runs in linear time.
     */
    private void buildHeap( )
    {
        for( int i = currentSize / 2; i > 0; i-- )
            percolateDown( i );
    }

    private static final int DEFAULT_CAPACITY = 100;

    private int currentSize;   // Number of elements in heap
    private T [ ] array; // The heap array
    //private Comparator<? super T> cmp;

    /**
     * Internal method to percolate down in the heap.
     * @param hole the index at which the percolate begins.
     */
    private void percolateDown( int hole )
    {
        int child;
        T tmp = array[ hole ];

        for( ; hole * 2 <= currentSize; hole = child )
        {
            child = hole * 2;
            if( child != currentSize &&
                    compare( array[ child + 1 ], array[ child ] ) < 0 )
                child++;
            if( compare( array[ child ], tmp ) < 0 )
                array[ hole ] = array[ child ];
            else
                break;
        }
        array[ hole ] = tmp;
    }

    /**
     * Internal method to extend array.
     */
    @SuppressWarnings("unchecked")
    private void doubleArray( )
    {
        T [ ] newArray;

        newArray = (T []) new Comparable[ array.length * 2 ];

        for( int i = 0; i < array.length; i++ )
            newArray[ i ] = array[ i ];
        array = newArray;
    }
}
