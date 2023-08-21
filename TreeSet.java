import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.SortedSet;

/**
 * A navigable set implemented as a Red-Black Tree.
 * 
 * Documentation is largely taken from the documentation for the
 * NavigableSet<E>, Set<E>, and Iterator<E> interfaces.
 * https://docs.oracle.com/javase/8/docs/api/java/util/NavigableSet.html
 * https://docs.oracle.com/javase/8/docs/api/java/util/Set.html
 * https://docs.oracle.com/javase/8/docs/api/java/util/Iterator.html
 */
public class TreeSet<E extends Comparable<E>> extends AbstractSet<E> implements NavigableSet<E> {
    TreeSetEntry root;
    int size = 0;
    Comparator<? super E> comparator;

    @Override
    public Comparator<? super E> comparator() {
        return comparator;
    }

    /**
     * Adds the specified element to this set if it is not already present.
     * 
     * @param e the element to add
     * 
     * @return true if the set is changed by the operation
     */
    public boolean add(E e) {
        if (size() == 0) {
            root = new TreeSetEntry(e);
            size++;
            rebalance(add);
            return true;
        }

        TreeSetEntry tmp_p = getParent(e);
        TreeSetEntry tmp = tmp_p == null ? root : tmp_p.getChild(e);
        if (tmp != null) {
            return false;
        }

        TreeSetEntry add = new TreeSetEntry(e);
        if (tmp_p.compare(e) > 0) {
            tmp_p.setLeft(add);
        } else if (tmp_p.compare(e) < 0) {
            tmp_p.setRight(add);
        }
        size++;
        rebalance(add);
        return true;
    }

    private void rebalanceInsert(E e) {
        if (!isEmpty()) {
            if (root.compare(e) == 0) {
                root.setBlack();
            } else {
                TreeSetEntry tmp_g = null;
                TreeSetEntry tmp_p = null;
                TreeSetEntry tmp_u = null;
                TreeSetEntry tmp_c = root;
                TreeSetEntry tmp_s = null;

                // find the child
                while (!(tmp_c.compare(e) == 0)) {
                    tmp_g = tmp_p;
                    tmp_p = tmp_c;
                    tmp_u = tmp_s;
                    if (tmp_c.compare(e) < 0) {
                        tmp_c = tmp_c.getLeft();
                        tmp_s = tmp_c.getRight();
                    } else {
                        tmp_c = tmp_c.getRight();
                        tmp_s = tmp_c.getLeft();
                    }
                }

                // red parent -- must be refactored
                if (tmp_p.isRed()) {
                    // red uncle -- simple recolor
                    if (tmp_u.isRed()) {
                        tmp_p.setBlack();
                        tmp_u.setBlack();
                        tmp_g.setRed();
                        rebalance(tmp_g.getData());
                    }
                    // black uncle -- requires rotation
                    else {
                        if (tmp_p.compare(tmp_g.getData()) < 0) {
                            // Left Left
                            if (tmp_p.compare(e) > 0) {
                                tmp_g.llr();
                                tmp_g.swapColor(tmp_p);
                            }
                            // Left Right
                            else {
                                tmp_g.lrr();
                                tmp_g.swapColor(tmp_c);
                            }
                        } else {
                            // Right Left
                            if (tmp_p.compare(e) > 0) {
                                tmp_g.rlr();
                                tmp_g.swapColor(tmp_c);
                            }
                            // Right Right
                            else {
                                tmp_g.rrr();
                                tmp_g.swapColor(tmp_p);
                            }
                        }
                    }
                }
            }
        }
    }

    private void rebalanceDelete(E e, boolean isRed) {
        if (!isEmpty()) {
            if (root.compare(e) == 0) {
                root.setBlack();
            } else {
                TreeSetEntry tmp_g = null;
                TreeSetEntry tmp_p = null;
                TreeSetEntry tmp_u = null;
                TreeSetEntry tmp_c = root;
                TreeSetEntry tmp_s = null;

                // find the child
                while (!(tmp_c.compare(e) == 0)) {
                    tmp_g = tmp_p;
                    tmp_p = tmp_c;
                    tmp_u = tmp_s;
                    if (tmp_c.compare(e) < 0) {
                        tmp_c = tmp_c.getLeft();
                        tmp_s = tmp_c.getRight();
                    } else {
                        tmp_c = tmp_c.getRight();
                        tmp_s = tmp_c.getLeft();
                    }
                }

                tmp_c.setBlack();
                if (!isRed) {

                }



    }

    /**
     * Adds all of the elements in the specified collection to this set.
     * 
     * @param c the collection to add the the set
     * 
     * @return true if the set is changed by the operation
     * 
     * @exception NullPointerException if collection is null
     */
    public boolean addAll(Collection<? extends E> c) {
        if (c == null) {
            throw new NullPointerException("Collection cannot be null");
        }

        boolean ret = false;
        for (E e : c) {
            ret |= add(e);
        }
        return ret;
    }

    /**
     * Removes a single instance of the specified element from this collection, if
     * it is present.
     * 
     * @param o the element to remove.
     * @return whether or not the element was present in the set.
     * 
     */
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (isEmpty()) {
            return false;
        } else if (root.compare(o) == 0) {
            rootRemove();
        } else {
            E element = null;
            try {
                element = (E) o;
            } catch (ClassCastException e) {
                return false;
            }

            TreeSetEntry tmp_p = getParent(element);
            TreeSetEntry tmp = tmp_p.getChild(element);
            if (tmp == null) {
                return false;
            }
            tmp_p.remove(tmp);
        }
        rebalance();
        size--;
        return true;
    }

    /**
     * Removes all of this collection's elements that are also contained in the
     * specified collection.
     * 
     * @param c the collection to remove
     * @return whether any elements were removed by this operation
     * 
     * @exception NullPointerException if the collection is null
     */
    public boolean removeAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException("Collection cannot be null.");
        }

        boolean ret = false;
        for (Object o : c) {
            ret |= remove(o);
        }
        return ret;
    }

    /**
     * Retains only the elements in this collection that are contained in the
     * specified collection.
     * 
     * @param c the collection to remove
     * @return whether any elements were removed by this operation
     * 
     * @exception NullPointerException if the collection is null
     */
    public boolean retainAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException("Collection cannot be null.");
        }

        Iterator<E> it = iterator();
        boolean ret = false;
        while (it.hasNext()) {
            if (!c.contains(it.next())) {
                it.remove();
                ret = true;
            }
        }
        return ret;
    }

    /**
     * Removes all of the elements from this map.
     */
    public void clear() {
        clearHelper(root);
        root = null;
        size = 0;
    }

    /**
     * Retrieves and removes the first element, or returns null if this set is
     * empty.
     * 
     * @return the element that was removed, or null.
     */
    public E pollFirst() {
        if (isEmpty()) {
            return null;
        }

        TreeSetEntry node = firstParent();
        TreeSetEntry ret;
        if (node == null) {
            ret = root;
            rootRemove();
        } else {
            ret = node.getLeft();
            node.remove(ret);
        }
        size--;
        rebalance();
        return ret.getData();
    }

    /**
     * Retrieves and removes the last element, or returns null if this set is empty.
     * 
     * @return the element that was removed, or null.
     */
    public E pollLast() {
        if (isEmpty()) {
            return null;
        }

        TreeSetEntry node = lastParent();
        TreeSetEntry ret;
        if (node == null) {
            ret = root;
            rootRemove();
        } else {
            ret = node.getRight();
            node.remove(ret);
        }
        size--;
        rebalance();
        return ret.getData();
    }

    /**
     * Returns the cardinality of this set.
     * 
     * @return the cardinality of this set.
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if this set contains no elements.
     * 
     * @return whether this set is empty.
     */
    public boolean isEmpty() {
        return size() == 0;
    }

    /**
     * Returns true if this set contains the specified element.
     * 
     * @param key the object to find
     * @return true if the set contains the element.
     */
    @SuppressWarnings("unchecked")
    public boolean contains(Object o) {
        if (isEmpty()) {
            return false;
        }

        E element = null;
        try {
            element = (E) o;
        } catch (ClassCastException e) {
            return false;
        }

        return root.compare(o) == 0 ? true : getParent(element).getChild(element) != null;
    }

    /**
     * Returns true if this set contains all of the elements of the specified
     * collection.
     * 
     * @param c the collection of elements to hav
     * @return true if the set contains all of the elements
     * 
     * @exception NullPointerException if collection is null.
     * @exception ClassCastException   if an element is of an inappopriate type.
     */
    public boolean containsAll(Collection<?> c) {
        if (c == null) {
            throw new NullPointerException("Collection cannot be null");
        }

        boolean ret = true;
        for (Object e : c) {
            ret &= contains(e);
        }
        return ret;
    }

    /**
     * Returns the first element in this set.
     * 
     * @return the first element
     * 
     * @exception NoSuchElementException if this set is empty
     */
    public E first() {
        if (isEmpty()) {
            throw new NoSuchElementException("Set is empty");
        }

        TreeSetEntry tmp_p = firstParent();
        return tmp_p == null ? root.getData() : tmp_p.getLeft().getData();
    }

    /**
     * Returns the last element in this set.
     * 
     * @return the last element
     * 
     * @exception NoSuchElementException if this set is empty
     */
    public E last() {
        if (isEmpty()) {
            throw new NoSuchElementException("Set is empty");
        }

        TreeSetEntry tmp_p = lastParent();
        return tmp_p == null ? root.getData() : tmp_p.getRight().getData();
    }

    /**
     * Returns the greatest element in this set strictly less than the given
     * element, or null if there is no such element.
     * 
     * @param e the given element
     * @return the greatest element less than the given element
     */
    public E lower(E e) {
        return downEntry(e, false).getData();
    }

    /**
     * Returns the greatest element in this set strictly greater than the given
     * element, or null if there is no such element.
     * 
     * @param e the given element
     * @return the least element greater than the given element
     */
    public E higher(E e) {
        return upEntry(e, false).getData();
    }

    /**
     * Returns the greatest element in this set less than or equal to the given
     * element, or null if there is no such element.
     * 
     * @param e the given element
     * @return the greatest element less than or equal to the given element
     */
    public E floor(E e) {
        return downEntry(e, true).getData();
    }

    /**
     * Returns the greatest element in this set greater than or equal to the given
     * element, or null if there is no such element.
     * 
     * @param e the given element
     * @return the least element greater than or equal to the given element
     */
    public E ceiling(E e) {
        return upEntry(e, true).getData();
    }

    @Override
    public Iterator<E> iterator() {
        return new TreeSetIterator();
    }

    @Override
    public NavigableSet<E> descendingSet() {
        return new DescendingTreeSet(this);
    }

    @Override
    public Iterator<E> descendingIterator() {
        return descendingSet().iterator();
    }

    /**
     * Returns a view of the portion of this set whose elements are strictly less
     * than toElement.
     * 
     * @param toElement the given element
     * @return a sub-view of the set
     */
    public SortedSet<E> headSet(E toElement) {
        return headSet(toElement, false);
    }

    /**
     * Returns a view of the portion of this set whose elements are strictly less
     * than (or equal to) toElement.
     * 
     * @param toElement the given element
     * @return a sub-view of the set
     */
    public NavigableSet<E> headSet(E toElement, boolean inclusive) {
        return subSet(first(), true, toElement, inclusive);
    }

    /**
     * Returns a view of the portion of this set whose elements range from
     * fromElement to toElement.
     * 
     * @param fromElement the element to start from (inclusive)
     * @param toElement   the element to end at (exclusive)
     * @return a sub-view of the set
     */
    public SortedSet<E> subSet(E fromElement, E toElement) {
        return subSet(fromElement, true, toElement, false);
    }

    /**
     * Returns a view of the portion of this set whose elements range from
     * fromElement to toElement.
     * 
     * @param fromElement   the element to start from
     * @param fromInclusive whether to include fromElement in the view
     * @param toElement     the element to end at
     * @param toInclusive   whether to include toElement in the view
     * @return a sub-view of the set
     */
    public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
        return new SubTreeSet(fromElement, fromInclusive, toElement, toInclusive);
    }

    /**
     * Returns a view of the portion of this set whose elements are greater than or
     * equal to than toElement.
     * 
     * @param toElement the given element
     * @return a sub-view of the set
     */
    public SortedSet<E> tailSet(E fromElement) {
        return tailSet(fromElement, true);
    }

    /**
     * Returns a view of the portion of this set whose elements are greater than (or
     * equal to) than toElement.
     * 
     * @param toElement the given element
     * @return a sub-view of the set
     */
    public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
        return subSet(fromElement, inclusive, last(), true);
    }

    /**
     * Returns an array containing all of the elements in this collection.
     * 
     * @return an array containing all the elements in this collection
     */
    public Object[] toArray() {
        Object[] ret = new Object[size()];

        int i = 0;
        Iterator<E> it = iterator();
        while (it.hasNext()) {
            ret[i++] = it.next();
        }
        return ret;
    }

    /**
     * Returns an array containing all of the elements in this collection; the
     * runtime type of the returned array is that of the specified array.
     * 
     * @param a the specified array
     * @return an array containing all the elements in this collection
     * 
     * @exception NullPointerException if array is null.
     * @exception ArrayStoreException  if T is not a supertype of E
     */
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a == null) {
            throw new NullPointerException("Array should not be null");
        }

        T[] ret = a;
        if (a.length < size()) {
            ret = (T[]) Arrays.copyOf(a, size, a.getClass());
        }

        int i = 0;
        Iterator<E> it = iterator();
        try {
            while (i < size() && it.hasNext()) {
                ret[i++] = (T) it.next();
            }
        } catch (ClassCastException e) {
            throw new ArrayStoreException("T is not a supertype of E");
        }
        if (a.length > size()) {
            ret[size()] = null;
        }
        return ret;
    }

    /**
     * Private helper methods
     */

    /**
     * Recursive helper method for clear()
     * 
     * @param current the node to clear
     */
    private void clearHelper(TreeSetEntry current) {
        if (current != null) {
            clearHelper(current.getLeft());
            clearHelper(current.getRight());

            current.setLeft(null);
            current.setRight(null);
        }
    }

    /**
     * A helper method to find an entry with a key less than or equal to the
     * parameter.
     * 
     * @param key       the key to reference
     * @param inclusive whether the selected key can be the parameter
     * @return an entry that is less than or equal to the parameter
     */
    private TreeSetEntry downEntry(E data, boolean inclusive) {
        TreeSetEntry save = null;
        TreeSetEntry tmp_p = null;
        TreeSetEntry tmp = root;
        while (tmp != null) {
            if (tmp.compare(data) > 0) {
                tmp_p = tmp;
                tmp = tmp.getLeft();
            } else if (tmp.compare(data) < 0) {
                save = tmp_p;
                tmp_p = tmp;
                tmp = tmp.getRight();
            } else {
                if (inclusive) {
                    return tmp;
                }

                if (!tmp.hasLeft()) {
                    return save;
                }

                tmp = tmp.getLeft();
                while (tmp.hasRight()) {
                    tmp = tmp.getRight();
                }
                return tmp;
            }
        }
        return tmp_p.compare(data) < 0 ? tmp_p : save;
    }

    /**
     * Helper method to find the parent of the first element
     * 
     * @return the parent of the key-value mapping
     */
    private TreeSetEntry firstParent() {
        TreeSetEntry tmp_p = null;
        if (!isEmpty()) {
            TreeSetEntry tmp = root;
            while (tmp.hasLeft()) {
                tmp_p = tmp;
                tmp = tmp.getLeft();
            }
        }
        return tmp_p;
    }

    /**
     * Helper function which finds the parent entry for a given key
     * 
     * @param o the object to reference
     * @return the entry which represents the parent entry
     */
    private TreeSetEntry getParent(E e) {
        TreeSetEntry tmp_p = null;
        TreeSetEntry tmp = root;

        while (tmp != null && (tmp.compare(e) != 0)) {
            tmp_p = tmp;
            tmp = tmp.compare(e) > 0 ? tmp.getLeft() : tmp.getRight();
        }
        return tmp_p;
    }

    /**
     * Helper method to find the parent of the last entry
     * 
     * @return the parent entry
     */
    private TreeSetEntry lastParent() {
        TreeSetEntry tmp_p = null;
        TreeSetEntry tmp = root;
        while (tmp.hasRight()) {
            tmp_p = tmp;
            tmp = tmp.getRight();
        }
        return tmp_p;
    }

    /**
     * Helper method to remove and replace the root of the tree.
     */
    private void rootRemove() {
        TreeSetEntry tmp = new TreeSetEntry(null);
        tmp.setLeft(root);

        tmp.remove(root);
        root = tmp.getLeft();

        tmp.setLeft(null);
        tmp = null;
    }

    /**
     * A helper method to find an element with a key greater than or equal to the
     * parameter.
     * 
     * @param key       the key to reference
     * @param inclusive whether the selected key can be the parameter
     * @return an entry that is greater than or equal to the parameter
     */
    private TreeSetEntry upEntry(E key, boolean inclusive) {
        TreeSetEntry save = null;
        TreeSetEntry tmp_p = null;
        TreeSetEntry tmp = root;
        while (tmp != null) {
            if (tmp.compare(key) < 0) {
                tmp_p = tmp;
                tmp = tmp.getRight();
            } else if (tmp.compare(key) > 0) {
                save = tmp_p;
                tmp_p = tmp;
                tmp = tmp.getLeft();
            } else {
                if (inclusive) {
                    return tmp;
                }

                if (!tmp.hasRight()) {
                    return save;
                }

                tmp = tmp.getRight();
                while (tmp.hasLeft()) {
                    tmp = tmp.getLeft();
                }
                return tmp;
            }
        }
        return tmp_p.compare(key) > 0 ? tmp_p : save;
    }

    private class TreeSetEntry {
        E data;
        TreeSetEntry left;
        TreeSetEntry right;
        boolean color;

        /**
         * @param data
         */
        public TreeSetEntry(E data) {
            this.data = data;
            color = false;
        }

        /**
         * Compares the specified object with this entry for equality.
         * 
         * @param o the object to compare
         * @return whether the object is equal
         */
        public boolean equals(Object o) {
            if (o == null) {
                return false;
            }

            try {
                return compare(o) == 0;
            } catch (ClassCastException e) {

            }
            return false;
        }

        /**
         * Checks if a left child exists.
         * 
         * @return if left child exists
         */
        public boolean hasLeft() {
            return left != null;
        }

        /**
         * Checks if a right child exists.
         * 
         * @return if right child exists
         */
        public boolean hasRight() {
            return right != null;
        }

        /**
         * Returns the data stored within this entry.
         * 
         * @return data
         */
        public E getData() {
            return data;
        }

        /**
         * Returns the left child of this entry.
         * 
         * @return the left child
         */
        protected TreeSetEntry getLeft() {
            return left;
        }

        /**
         * Returns the right child of this entry.
         * 
         * @return the right child
         */
        protected TreeSetEntry getRight() {
            return right;
        }

        /**
         * Replaces the left child of this entry.
         * 
         * @param left the new left child to store
         * @return the left child
         */
        protected TreeSetEntry setLeft(TreeSetEntry left) {
            TreeSetEntry ret = this.left;
            this.left = left;
            return ret;
        }

        /**
         * Replaces the right child of this entry.
         * 
         * @param the new right child to store
         * @return the right child
         */
        protected TreeSetEntry setRight(TreeSetEntry right) {
            TreeSetEntry ret = this.right;
            this.right = right;
            return ret;
        }

        /**
         * Remove a child from this TreeMapEntry, and adjust its other children
         * accordingly.
         * 
         * @param child the child to remove.
         */
        protected void remove(TreeSetEntry child) {
            if (getChild(child.data) == null) {
                throw new IllegalArgumentException("Cannot remove child that does not exist.");
            }

            if (child.hasLeft() && child.hasRight()) {
                TreeSetEntry tmp_p = child;
                TreeSetEntry tmp = child.getLeft();
                while (tmp.hasRight()) {
                    tmp_p = tmp;
                    tmp = tmp.getRight();
                }
                data = tmp.getData();
                tmp_p.remove(tmp);
            } else {
                if (child.hasLeft()) {
                    if (getLeft() != null && getLeft().equals(child)) {
                        setLeft(child.getLeft());
                    } else {
                        setRight(child.getLeft());
                    }
                    child.setLeft(null);
                } else if (child.hasRight()) {
                    if (getLeft() != null && getLeft().equals(child)) {
                        setLeft(child.getRight());
                    } else {
                        setRight(child.getRight());
                    }
                    child.setRight(null);
                } else {
                    if (getLeft() != null && getLeft().equals(child)) {
                        setLeft(null);
                    } else {
                        setRight(null);
                    }
                }
            }
        }

        /**
         * Return the child of the entry with a specific key, if one exists.
         * 
         * @param key the key to reference
         * @return the child if it exists, else null
         */

        protected TreeSetEntry getChild(E data) {
            if (hasLeft() && getLeft().compare(data) == 0) {
                return getLeft();
            } else if (hasRight() && getRight().compare(data) == 0) {
                return getRight();
            }
            return null;
        }

        /**
         * Compare this entry to an object based on its key.
         * 
         * @param o the object to compare
         * @return the comparison between this and o
         * 
         * @exception ClassCastException if object is not a supertype of E
         */
        @SuppressWarnings("unchecked")
        protected int compare(Object o) {
            if (getData() == null && o == null) {
                return 0;
            }

            E k = (E) o;

            if (comparator != null) {
                return comparator.compare(getData(), k);
            }

            return getData().compareTo(k);
        }
    }

    private class TreeSetIterator implements Iterator<E> {
        E next;
        E toRemove;

        boolean lastElement;
        boolean noRemove;

        public TreeSetIterator() {
            next = first();
            lastElement = false;
            noRemove = true;
        }

        @Override
        public boolean hasNext() {
            return lastElement;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new IllegalStateException("No next exists.");
            }
            if (next == null ? last() == null : next.equals(last())) {
                lastElement = true;
            }
            toRemove = next;
            noRemove = false;
            next = higher(next);
            return toRemove;
        }

        @Override
        public void remove() {
            if (noRemove) {
                throw new IllegalStateException("Cannot remove at this time.");
            }
            TreeSet.this.remove(toRemove);
            noRemove = true;
        }
    }

    private class DescendingTreeSet implements NavigableSet<E> {
        NavigableSet<E> treeSet;

        public DescendingTreeSet(NavigableSet<E> set) {
            treeSet = set;
        }

        @Override
        public Comparator<? super E> comparator() {
            return treeSet.comparator().reversed();
        }

        @Override
        public E first() {
            return treeSet.last();
        }

        @Override
        public E last() {
            return treeSet.first();
        }

        @Override
        public int size() {
            return treeSet.size();
        }

        @Override
        public boolean isEmpty() {
            return treeSet.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return treeSet.contains(o);
        }

        @Override
        public Object[] toArray() {
            Object[] ret = new Object[size()];

            int i = 0;
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                ret[i++] = it.next();
            }
            return ret;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            if (a == null) {
                throw new NullPointerException("Array should not be null");
            }

            T[] ret = a;
            if (a.length < size()) {
                ret = (T[]) Arrays.copyOf(a, size, a.getClass());
            }

            int i = 0;
            Iterator<E> it = iterator();
            try {
                while (i < size() && it.hasNext()) {
                    ret[i++] = (T) it.next();
                }
            } catch (ClassCastException e) {
                throw new ArrayStoreException("T is not a supertype of E");
            }
            if (a.length > size()) {
                ret[size()] = null;
            }
            return ret;
        }

        @Override
        public boolean add(E e) {
            return treeSet.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return treeSet.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return treeSet.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            return treeSet.addAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return treeSet.retainAll(c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return treeSet.removeAll(c);
        }

        @Override
        public void clear() {
            treeSet.clear();
        }

        @Override
        public E lower(E e) {
            return treeSet.higher(e);
        }

        @Override
        public E floor(E e) {
            return treeSet.ceiling(e);
        }

        @Override
        public E ceiling(E e) {
            return treeSet.floor(e);
        }

        @Override
        public E higher(E e) {
            return treeSet.lower(e);
        }

        @Override
        public E pollFirst() {
            return treeSet.pollLast();
        }

        @Override
        public E pollLast() {
            return treeSet.pollFirst();
        }

        @Override
        public Iterator<E> iterator() {
            return new DescendingTreeSetIterator();
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return treeSet;
        }

        @Override
        public Iterator<E> descendingIterator() {
            return treeSet.iterator();
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            return new DescendingTreeSet(treeSet.subSet(fromElement, fromInclusive, toElement, toInclusive));
        }

        @Override
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return subSet(first(), true, toElement, inclusive);
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return subSet(fromElement, inclusive, last(), true);
        }

        @Override
        public SortedSet<E> subSet(E fromElement, E toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        @Override
        public SortedSet<E> headSet(E toElement) {
            return headSet(toElement, false);
        }

        @Override
        public SortedSet<E> tailSet(E fromElement) {
            return tailSet(fromElement, true);
        }

        private class DescendingTreeSetIterator implements Iterator<E> {
            E next;
            E toRemove;

            boolean lastElement;
            boolean noRemove;

            public DescendingTreeSetIterator() {
                next = first();
                lastElement = false;
                noRemove = true;
            }

            @Override
            public boolean hasNext() {
                return lastElement;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new IllegalStateException("No next exists.");
                }
                if (next == null ? last() == null : next.equals(last())) {
                    lastElement = true;
                }
                toRemove = next;
                noRemove = false;
                next = higher(next);
                return toRemove;
            }

            @Override
            public void remove() {
                if (noRemove) {
                    throw new IllegalStateException("Cannot remove at this time.");
                }
                DescendingTreeSet.this.remove(toRemove);
                noRemove = true;
            }
        }
    }

    public class SubTreeSet implements NavigableSet<E> {
        TreeSet<E> treeSet;

        E fromElement;
        boolean fromInclusive;
        E toElement;
        boolean toInclusive;s

        public SubTreeSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            this.fromElement = fromElement;
            this.fromInclusive = fromInclusive;
            this.toElement = toElement;
            this.toInclusive = toInclusive;
        }

        private boolean inBounds(Object o) {
            E e = (E) o;

            if (fromInclusive ? compare(e, fromElement) < 0 : compare(e, fromElement) <= 0) {
                return false;
            } else if (toInclusive ? compare(toElement, e) <= 0 : compare(toElement, e) < 0) {
                return false;
            }
            return true;
        }

        private int compare(E lhs, E rhs) {
            if (lhs == null && rhs == null) {
                return 0;
            }
            if (comparator() != null) {
                return comparator().compare(lhs, rhs);
            }
            return lhs.compareTo(rhs);
        }

        @Override
        public Comparator<? super E> comparator() {
            return treeSet.comparator();
        }

        @Override
        public E first() {
            return fromInclusive ? treeSet.ceiling(fromElement) : treeSet.higher(fromElement);
        }

        @Override
        public E last() {
            return toInclusive ? treeSet.floor(toElement) : treeSet.lower(toElement);
        }

        @Override
        public int size() {
            Iterator<E> it = iterator();
            int ret = 0;
            while (it.hasNext()) {
                ret++;
            }
            return ret;
        }

        @Override
        public boolean isEmpty() {
            return size() == 0;
        }

        @Override
        public boolean contains(Object o) {
            if (!inBounds(o)) {
                return false;
            }
            return treeSet.contains(o);
        }

        @Override
        public Object[] toArray() {
            Object[] ret = new Object[size()];

            int i = 0;
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                ret[i++] = it.next();
            }
            return ret;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T[] toArray(T[] a) {
            if (a == null) {
                throw new NullPointerException("Array should not be null");
            }

            T[] ret = a;
            if (a.length < size()) {
                ret = (T[]) Arrays.copyOf(a, size, a.getClass());
            }

            int i = 0;
            Iterator<E> it = iterator();
            try {
                while (i < size() && it.hasNext()) {
                    ret[i++] = (T) it.next();
                }
            } catch (ClassCastException e) {
                throw new ArrayStoreException("T is not a supertype of E");
            }
            if (a.length > size()) {
                ret[size()] = null;
            }
            return ret;
        }

        @Override
        public boolean add(E e) {
            if (!inBounds(e)) {
                throw new IllegalArgumentException("Out-of-bounds element: " + e);
            }

            return treeSet.add(e);
        }

        @Override
        public boolean remove(Object o) {
            if (!inBounds(o)) {
                return false;
            }

            E e = (E) o;

            return treeSet.add(e);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            if (c == null) {
                throw new NullPointerException("Collection cannot be null");
            }

            boolean ret = true;
            for (Object e : c) {
                ret &= contains(e);
            }
            return ret;
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            if (c == null) {
                throw new NullPointerException("Collection cannot be null");
            }

            boolean ret = false;
            for (E e : c) {
                ret |= add(e);
            }
            return ret;
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            if (c == null) {
                throw new NullPointerException("Collection cannot be null.");
            }

            Iterator<E> it = iterator();
            boolean ret = false;
            while (it.hasNext()) {
                if (!c.contains(it.next())) {
                    it.remove();
                    ret = true;
                }
            }
            return ret;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            if (c == null) {
                throw new NullPointerException("Collection cannot be null.");
            }

            boolean ret = false;
            for (Object o : c) {
                ret |= remove(o);
            }
            return ret;
        }

        @Override
        public void clear() {
            Iterator<E> it = iterator();
            while (it.hasNext()) {
                it.next();
                it.remove();
            }
        }

        @Override
        public E lower(E e) {
            if (!inBounds(e)) {
                if (compare(toElement, e) <= 0) {
                    return last();
                } else {
                    return null;
                }
            }
            return treeSet.floor(e);
        }

        @Override
        public E floor(E e) {
            if (!inBounds(e)) {
                if (compare(toElement, e) < 0) {
                    return last();
                } else {
                    return null;
                }
            }
            return treeSet.floor(e);
        }

        @Override
        public E ceiling(E e) {
            if (!inBounds(e)) {
                if (compare(e, fromElement) <= 0) {
                    return first();
                } else {
                    return null;
                }
            }
            return treeSet.ceiling(e);
        }

        @Override
        public E higher(E e) {
            if (!inBounds(e)) {
                if (compare(e, fromElement) < 0) {
                    return first();
                } else {
                    return null;
                }
            }
            return treeSet.ceiling(e);
        }

        @Override
        public E pollFirst() {
            E ret = first();
            remove(ret);
            return ret;
        }

        @Override
        public E pollLast() {
            E ret = last();
            remove(ret);
            return ret;
        }

        @Override
        public Iterator<E> iterator() {
            return new SubTreeSetIterator();
        }

        @Override
        public NavigableSet<E> descendingSet() {
            return new DescendingTreeSet(this);
        }

        @Override
        public Iterator<E> descendingIterator() {
            return descendingSet().iterator();
        }

        @Override
        public NavigableSet<E> subSet(E fromElement, boolean fromInclusive, E toElement, boolean toInclusive) {
            if (!inBounds(fromElement) || !inBounds(toElement)) {
                throw new IllegalArgumentException("Out-of-bounds range");
            }

            return treeSet.subSet(fromElement, fromInclusive, toElement, toInclusive);
        }

        @Override
        public NavigableSet<E> headSet(E toElement, boolean inclusive) {
            return subSet(first(), true, toElement, inclusive);
        }

        @Override
        public NavigableSet<E> tailSet(E fromElement, boolean inclusive) {
            return subSet(fromElement, inclusive, last(), true);
        }

        @Override
        public SortedSet<E> subSet(E fromElement, E toElement) {
            return subSet(fromElement, true, toElement, false);
        }

        @Override
        public SortedSet<E> headSet(E toElement) {
            return headSet(toElement, false);
        }

        @Override
        public SortedSet<E> tailSet(E fromElement) {
            return tailSet(fromElement, true);
        }

        private class SubTreeSetIterator implements Iterator<E> {
            E next;
            E toRemove;

            boolean lastElement;
            boolean noRemove;

            public SubTreeSetIterator() {
                next = first();
                lastElement = false;
                noRemove = true;
            }

            @Override
            public boolean hasNext() {
                return lastElement;
            }

            @Override
            public E next() {
                if (!hasNext()) {
                    throw new IllegalStateException("No next exists.");
                }
                if (next == null ? last() == null : next.equals(last())) {
                    lastElement = true;
                }
                toRemove = next;
                noRemove = false;
                next = higher(next);
                return toRemove;
            }

            @Override
            public void remove() {
                if (noRemove) {
                    throw new IllegalStateException("Cannot remove at this time.");
                }
                SubTreeSet.this.remove(toRemove);
                noRemove = true;
            }
        }

    }
}