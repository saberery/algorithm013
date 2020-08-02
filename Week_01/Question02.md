## 分析 Queue 和 Priority Queue 的源码
JDK11

### Queue是一个接口
```java
public interface Queue<E> extends Collection<E> {

    // 入队，无法入队时扩容或返回false
    boolean offer(E e);
    // 入队，无法入队时扩容或抛异常
    boolean add(E e);
    // 出队，无法出队时返回null，不阻塞
    E poll();
    // 出队，无法出队时抛异常，不阻塞
    E remove();
    // 查看队头元素，如果队列为空，抛出异常
    E element();
    // 查看队头元素，如果队列为空，返回null
    E peek();
}
```       


### Priority Queue是具体实现类，继承AbstractQueue
是一个有序队列，自动对传入的值进行排序
offer,add和poll,remove的时间复杂度是log(n) //add本质上是调用offer,remove本质上是调用poll.remove在AbstractQueue定义
remove(Object)和contains(Object)的时间复杂度是O(n) //遍历queue的地址，找到响应值的索引
peek, element和size的时间复杂度O(1) // element本质上也是调用peek,element在AbstractQueue定义

```java

public class PriorityQueue<E> extends AbstractQueue<E> implements Serializable {
    // 优先队列的默认初始容量
    private static final int DEFAULT_INITIAL_CAPACITY = 11;
    
    private static final int MAX_ARRAY_SIZE = Integer.MAX_VALUE - 8;
    
    // 外部比较器，支持以自定义的顺序来比较元素。如果没有设置，则使用元素自身实现的内部比较器
    private final Comparator<? super E> comparator;
    
    // 存储队列元素
    transient Object[] queue; // non-private to simplify nested class access
    
    // 队列长度
    int size;
    
    // 记录队列结构的变动次数
    transient int modCount;     // non-private to simplify nested class access

    //构造器
    public PriorityQueue() {
        this(DEFAULT_INITIAL_CAPACITY, null);
    }
    public PriorityQueue(int initialCapacity) {
        this(initialCapacity, null);
    }
    public PriorityQueue(Comparator<? super E> comparator) {
        this(DEFAULT_INITIAL_CAPACITY, comparator);
    }
    public PriorityQueue(int initialCapacity, Comparator<? super E> comparator) {
        // Note: This restriction of at least one is not actually needed, but continues for 1.5 compatibility
        if(initialCapacity<1) {
            throw new IllegalArgumentException();
        }
        this.queue = new Object[initialCapacity];
        this.comparator = comparator;
    }
    public PriorityQueue(PriorityQueue<? extends E> c) {
        this.comparator = (Comparator<? super E>) c.comparator();
        // 用指定的优先队列中的元素初始化当前优先队列
        initFromPriorityQueue(c);
    }
    public PriorityQueue(SortedSet<? extends E> c) {
        this.comparator = (Comparator<? super E>) c.comparator();
        // 用指定容器中的元素初始化优先队列
        initElementsFromCollection(c);
    }
    public PriorityQueue(Collection<? extends E> c) {
        if(c instanceof SortedSet<?>) {
            SortedSet<? extends E> ss = (SortedSet<? extends E>) c;
            this.comparator = (Comparator<? super E>) ss.comparator();
            // 用指定容器中的元素初始化优先队列
            initElementsFromCollection(ss);
        } else if(c instanceof PriorityQueue<?>) {
            PriorityQueue<? extends E> pq = (PriorityQueue<? extends E>) c;
            this.comparator = (Comparator<? super E>) pq.comparator();
            // 用指定的优先队列中的元素初始化当前优先队列
            initFromPriorityQueue(pq);
        } else {
            this.comparator = null;
            // 用指定容器中的元素初始化优先队列，并重建小顶堆
            initFromCollection(c);
        }
    }
    // 入队，非线程安全，无法入队时扩容
    public boolean offer(E e) {
        if(e == null) {
            throw new NullPointerException();
        }
        
        modCount++;
        
        int i = size;
        if(i >= queue.length) {
            grow(i + 1);
        }
        
        // 插入。需要从小顶堆的结点i开始，向【上】查找一个合适的位置插入x
        siftUp(i, e);
        
        // 队列长度增一
        size = i + 1;
        
        return true;
    }
    // 入队/添加，非线程安全。无法入队时扩容
    public boolean add(E e) {
        return offer(e);
    }
    // 出队，非线程安全，无法出队时返回null
    public E poll() {
        final Object[] es = queue;
        
        // 获取队头元素
        final E result = (E)es[0];
        
        // 如果队头元素不为空
        if(result!= null) {
            modCount++;
            
            final int n = --size;
            
            // 摘下队尾元素
            final E x = (E) es[n];
            es[n] = null;
            
            // 插入。需要从小顶堆的根结点开始，向【下】查找一个合适的位置插入队尾元素
            if(n>0) {
                final Comparator<? super E> cmp;
                if((cmp = comparator) == null) {
                    siftDownComparable(0, x, es, n);
                } else {
                    siftDownUsingComparator(0, x, es, n, cmp);
                }
            }
        }
        
        return result;
    }
    // 移除，非线程安全，移除成功则返回true
    public boolean remove(Object o) {
        // 获取指定元素在队列中的索引，不在队列中时返回-1
        int i = indexOf(o);
        
        // 如果元素不在队列中，返回false
        if(i == -1) {
            return false;
        }
        
        // 移除队列索引i处的元素。如果小顶堆中的元素顺序发生了改变，则返回队尾元素
        removeAt(i);
        
        return true;
    }
    // 移除队列索引i处的元素。如果小顶堆中的元素顺序发生了改变，则返回队尾元素
    E removeAt(int i) {
        // assert i >= 0 && i < size;
        final Object[] es = queue;
        
        modCount++;
        
        int s = --size;
        
        // 如果移除的是队尾元素
        if(s == i) {
            // removed last element
            es[i] = null;
        } else {
            /* 如果不是移除队尾元素，则需要将队尾元素防止在新的小顶堆中的合适位置 */
            
            // 摘下队尾元素
            E moved = (E) es[s];
            
            es[s] = null;
            
            // 插入。需要从小顶堆的结点i开始，向【下】查找一个合适的位置插入moved
            siftDown(i, moved);
            
            /*
             * 如果待moved是以i为根结点的小顶堆上的最小值，
             * 那么不能保证moved比结点i的父结点元素更大，
             * 此时需要向上搜寻moved的一个合适的插入位置
             */
            if(es[i] == moved) {
                // 插入。需要从小顶堆的结点i开始，向【上】查找一个合适的位置插入moved
                siftUp(i, moved);
                
                /*
                 * 如果i处的新元素不是队尾元素，
                 * 说明小顶堆中的元素顺序发生了改变（不考虑i处的变化），
                 * 此时返回队尾元素
                 */
                if(es[i] != moved) {
                    return moved;
                }
            }
        }
        
        return null;
    }
    // 移除所有满足过滤条件的元素（非线程安全）
    public boolean removeIf(Predicate<? super E> filter) {
        Objects.requireNonNull(filter);
        return bulkRemove(filter);
    }
    // (匹配则移除)移除队列中所有与给定容器中的元素匹配的元素（非线程安全）
    public boolean removeAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return bulkRemove(e -> c.contains(e));
    }
    // (不匹配则移除)移除队列中所有与给定容器中的元素不匹配的元素（非线程安全）
    public boolean retainAll(Collection<?> c) {
        Objects.requireNonNull(c);
        return bulkRemove(e -> !c.contains(e));
    }
    // 清空，即移除所有元素（非线程安全）
    public void clear() {
        modCount++;
        final Object[] es = queue;
        for(int i = 0, n = size; i<n; i++) {
            es[i] = null;
        }
        size = 0;
    }
    // 获取队头元素
    public E peek() {
        return (E) queue[0];
    }
    // 判断队列中是否包含元素o
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }
    public Object[] toArray() {
        return Arrays.copyOf(queue, size);
    }
    public <T> T[] toArray(T[] a) {
        final int size = this.size;
        if(a.length<size)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(queue, size, a.getClass());
        System.arraycopy(queue, 0, a, 0, size);
        if(a.length>size)
            a[size] = null;
        return a;
    }
    // 遍历所有元素，并执行相应的择取操作
    public void forEach(Consumer<? super E> action) {
        Objects.requireNonNull(action);
        
        final int expectedModCount = modCount;
        final Object[] es = queue;
        
        for(int i = 0, n = size; i<n; i++) {
            action.accept((E) es[i]);
        }
        
        if(expectedModCount != modCount) {
            throw new ConcurrentModificationException();
        }
    }
    // 返回当前队列的迭代器
    public Iterator<E> iterator() {
        return new Itr();
    }
    // 返回描述此队列中元素的Spliterator
    public final Spliterator<E> spliterator() {
        return new PriorityQueueSpliterator(0, -1, 0);
    }
    // 返回队列中元素数量
    public int size() {
        return size;
    }
    // 返回该队列使用的外部比较器
    public Comparator<? super E> comparator() {
        return comparator;
    }
    private void writeObject(java.io.ObjectOutputStream s) throws IOException {
        // Write out element count, and any hidden stuff
        s.defaultWriteObject();
        
        // Write out array length, for compatibility with 1.5 version
        s.writeInt(Math.max(2, size + 1));
        
        // Write out all elements in the "proper order".
        final Object[] es = queue;
        for(int i = 0, n = size; i<n; i++)
            s.writeObject(es[i]);
    }
    private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
        // Read in size, and any hidden stuff
        s.defaultReadObject();
        
        // Read in (and discard) array length
        s.readInt();
        
        SharedSecrets.getJavaObjectInputStreamAccess().checkArray(s, Object[].class, size);
        final Object[] es = queue = new Object[Math.max(size, 1)];
        
        // Read in all elements.
        for(int i = 0, n = size; i<n; i++)
            es[i] = s.readObject();
        
        // Elements are guaranteed to be in "proper order", but the
        // spec has never explained what that might be.
        heapify();
    }
    // 用指定的优先队列中的元素初始化当前优先队列
    private void initFromPriorityQueue(PriorityQueue<? extends E> c) {
        if(c.getClass() == PriorityQueue.class) {
            this.queue = ensureNonEmpty(c.toArray());
            this.size = c.size();
        } else {
            // 用指定容器中的元素初始化优先队列，并重建小顶堆
            initFromCollection(c);
        }
    }
    // 用指定容器中的元素初始化优先队列，并重建小顶堆
    private void initFromCollection(Collection<? extends E> c) {
        // 用指定容器中的元素初始化优先队列
        initElementsFromCollection(c);
        // 重建小顶堆
        heapify();
    }
    // 用指定容器中的元素初始化优先队列
    private void initElementsFromCollection(Collection<? extends E> c) {
        // 获取容器中的元素
        Object[] es = c.toArray();
        
        int len = es.length;
        
        // If c.toArray incorrectly doesn't return Object[], copy it.
        if(es.getClass() != Object[].class) {
            es = Arrays.copyOf(es, len, Object[].class);
        }
        
        // 确保优先队列中的元素不为空
        if(len == 1 || this.comparator != null) {
            for(Object e : es) {
                if(e == null) {
                    throw new NullPointerException();
                }
            }
        }
        
        this.queue = ensureNonEmpty(es);
        this.size = len;
    }
    // 确保返回的数组长度>=1
    private static Object[] ensureNonEmpty(Object[] es) {
        return (es.length>0) ? es : new Object[1];
    }
    // 插入。需要从小顶堆的结点i开始，向【上】查找一个合适的位置插入x
    private void siftUp(int i, E x) {
        if(comparator == null) {
            siftUpComparable(i, x, queue);
        } else {
            siftUpUsingComparator(i, x, queue, comparator);
        }
    }
    // 插入。需要从小顶堆的结点i开始，向【下】查找一个合适的位置插入x
    private void siftDown(int i, E x) {
        if(comparator == null) {
            siftDownComparable(i, x, queue, size);
        } else {
            siftDownUsingComparator(i, x, queue, size, comparator);
        }
    }
    // 插入。需要从小顶堆的结点i开始，向上查找一个合适的位置插入x
    private static <T> void siftUpComparable(int i, T x, Object[] es) {
        // 类型转换，要求待插入元素必须实现Comparable接口
        Comparable<? super T> key = (Comparable<? super T>) x;
        
        while(i>0) {
            // 获取父结点索引
            int parent = (i - 1) >>> 1;
            
            // 父结点
            Object e = es[parent];
            
            // 如果待插入元素大于父节点中的元素，则退出循环
            if(key.compareTo((T) e) >= 0) {
                break;
            }
            
            // 子结点保存父结点中的元素
            es[i] = e;
            
            // 向上搜寻合适的插入位置
            i = parent;
        }
        
        // 将元素key插入到合适的位置
        es[i] = key;
    }
    // 插入。需要从小顶堆的结点i开始，向上查找一个合适的位置插入x
    private static <T> void siftUpUsingComparator(int i, T x, Object[] es, Comparator<? super T> cmp) {
        while(i>0) {
            // 获取父结点索引
            int parent = (i - 1) >>> 1;
            
            // 父结点
            Object e = es[parent];
            
            // 如果待插入元素大于父节点中的元素，则退出循环
            if(cmp.compare(x, (T) e) >= 0) {
                break;
            }
            
            // 子结点保存父结点中的元素
            es[i] = e;
            
            // 向上搜寻合适的插入位置
            i = parent;
        }
        
        // 将元素x插入到合适的位置
        es[i] = x;
    }
    // 插入。需要从小顶堆的结点i开始，向下查找一个合适的位置插入x
    private static <T> void siftDownComparable(int i, T x, Object[] es, int n) {
        // 类型转换，要求待插入元素必须实现Comparable接口
        Comparable<? super T> key = (Comparable<? super T>) x;
        
        // 最多搜索一半元素
        int half = n >>> 1;           // loop while a non-leaf
        
        while(i<half) {
            int min = (i << 1) + 1;   // 左结点索引
            int right = min + 1;      // 右结点索引
            
            // 假设左结点为较小的结点
            Object c = es[min];
            
            // 如果右结点更小
            if(right<n && ((Comparable<? super T>) c).compareTo((T) es[right])>0) {
                // 更新min指向子结点中较小的结点
                c = es[min = right];
            }
            
            // 如果待插入元素小于子结点中较小的元素，则退出循环
            if(key.compareTo((T) c)<=0) {
                break;
            }
            
            // 父结点位置保存子结点中较小的元素
            es[i] = c;
            
            // 向下搜寻合适的插入位置
            i = min;
        }
        
        // 将元素key插入到合适的位置
        es[i] = key;
    }
    // 插入。需要从小顶堆的结点i开始，向下查找一个合适的位置插入x
    private static <T> void siftDownUsingComparator(int i, T x, Object[] es, int n, Comparator<? super T> cmp) {
        // 最多搜索一半元素
        int half = n >>> 1;
        
        while(i<half) {
            int min = (i << 1) + 1;     // 左结点索引
            int right = min + 1;        // 右结点索引
            
            // 假设左结点为较小的结点
            Object c = es[min];
            
            // 如果右结点更小
            if(right<n && cmp.compare((T) c, (T) es[right])>0) {
                // 更新min指向子结点中较小的结点
                c = es[min = right];
            }
            
            // 如果待插入元素小于子结点中较小的元素，则退出循环
            if(cmp.compare(x, (T) c)<=0) {
                break;
            }
            
            // 父结点位置保存子结点中较小的元素
            es[i] = c;
            
            // 向下搜寻合适的插入位置
            i = min;
        }
        
        // 将元素x插入到合适的位置
        es[i] = x;
    }
    // 重建小顶堆
    private void heapify() {
        final Object[] es = queue;
        
        int n = size;
        
        // 从中间开始，倒着往回遍历
        int i = (n >>> 1) - 1;
        
        final Comparator<? super E> cmp;
        
        /* 插入。需要从小顶堆的结点i开始，向【下】查找一个合适的位置插入es[i] */
        
        if((cmp = comparator) == null) {
            for(; i >= 0; i--) {
                siftDownComparable(i, es[i], es, n);
            }
        } else {
            for(; i >= 0; i--) {
                siftDownUsingComparator(i, (E) es[i], es, n, cmp);
            }
        }
    }
    // 扩容
    private void grow(int minCapacity) {
        int oldCapacity = queue.length;
        // Double size if small; else grow by 50%
        int newCapacity = oldCapacity + ((oldCapacity<64) ? (oldCapacity + 2) : (oldCapacity >> 1));
        // overflow-conscious code
        if(newCapacity - MAX_ARRAY_SIZE>0) {
            newCapacity = hugeCapacity(minCapacity);
        }
        queue = Arrays.copyOf(queue, newCapacity);
    }
    private static int hugeCapacity(int minCapacity) {
        if(minCapacity<0) {
            // overflow
            throw new OutOfMemoryError();
        }
        
        return (minCapacity>MAX_ARRAY_SIZE) ? Integer.MAX_VALUE : MAX_ARRAY_SIZE;
    }
    // 获取指定元素在队列中的索引，不在队列中时返回-1
    private int indexOf(Object o) {
        if(o != null) {
            final Object[] es = queue;
            for(int i = 0, n = size; i<n; i++) {
                if(o.equals(es[i])) {
                    return i;
                }
            }
        }
        return -1;
    }
    // 移除指定元素
    void removeEq(Object o) {
        final Object[] es = queue;
        for(int i = 0, n = size; i<n; i++) {
            if(o == es[i]) {
                // 移除队列索引i处的元素。如果小顶堆中的元素顺序发生了改变，则返回队尾元素
                removeAt(i);
                break;
            }
        }
    }
    // 批量移除元素，即满足过滤条件的元素将被移除（非线程安全）
    private boolean bulkRemove(Predicate<? super E> filter) {
        final int expectedModCount = ++modCount;
        final Object[] es = queue;
        final int end = size;
        int i;
        
        // 跳过开头可以保留的元素
        for(i = 0; i<end && !filter.test((E) es[i]); i++)
            ;
        
        // 所有元素均可保留
        if(i >= end) {
            if(modCount != expectedModCount) {
                // 如果结构发生了变动，抛异常
                throw new ConcurrentModificationException();
            }
            return false;
        }
        
        // Tolerate predicates that reentrantly access the collection for
        // read (but writers still get CME), so traverse once to find
        // elements to delete, a second pass to physically expunge.
        final int beg = i;
        final long[] deathRow = nBits(end - beg);
        deathRow[0] = 1L;   // set bit 0
        for(i = beg + 1; i<end; i++) {
            if(filter.test((E) es[i])) {
                setBit(deathRow, i - beg);
            }
        }
        
        if(modCount != expectedModCount) {
            throw new ConcurrentModificationException();
        }
        
        int w = beg;
        for(i = beg; i<end; i++) {
            if(isClear(deathRow, i - beg)) {
                es[w++] = es[i];
            }
        }
        
        for(i = size = w; i<end; i++) {
            es[i] = null;
        }
        
        // 重建小顶堆
        heapify();
        
        return true;
    }
    
    private static long[] nBits(int n) {
        return new long[((n - 1) >> 6) + 1];
    }
    
    private static void setBit(long[] bits, int i) {
        bits[i >> 6] |= 1L << i;
    }
    
    private static boolean isClear(long[] bits, int i) {
        return (bits[i >> 6] & (1L << i)) == 0;
    }
    // 用于当前队列的外部迭代器
    private final class Itr implements Iterator<E> {
        /**
         * Index (into queue array) of element to be returned by
         * subsequent call to next.
         */
        private int cursor;         // 当前将要从优先队列中访问的元素的索引
        
        /**
         * Index of element returned by most recent call to next,
         * unless that element came from the forgetMeNot list.
         * Set to -1 if element is deleted by a call to remove.
         */
        private int lastRet = -1;   // 上次从优先队列中访问过的元素的索引
        
        /**
         * A queue of elements that were moved from the unvisited portion of
         * the heap into the visited portion as a result of "unlucky" element
         * removals during the iteration.  (Unlucky element removals are those
         * that require a siftup instead of a siftdown.)  We must visit all of
         * the elements in this list to complete the iteration.  We do this
         * after we've completed the "normal" iteration.
         *
         * We expect that most iterations, even those involving removals,
         * will not need to store elements in this field.
         */
        // 记录遍历过程中移除的队尾结点，以便稍后遍历
        private ArrayDeque<E> forgetMeNot;
        
        /**
         * Element returned by the most recent call to next iff that
         * element was drawn from the forgetMeNot list.
         */
        private E lastRetElt;       // 上次从forgetMeNot中访问过的元素
        
        /**
         * The modCount value that the iterator believes that the backing
         * Queue should have.  If this expectation is violated, the iterator
         * has detected concurrent modification.
         */
        private int expectedModCount = modCount;
        
        Itr() {
            // prevent access constructor creation
        }
        
        public boolean hasNext() {
            /*
             * 1.检查优先队列
             * 2.检查forgetMeNot容器
             */
            return cursor<size
                || (forgetMeNot != null && !forgetMeNot.isEmpty());
        }
        
        public E next() {
            if(expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            
            // 从优先队列中获取元素
            if(cursor<size) {
                return (E) queue[lastRet = cursor++];
            }
            
            // 从forgetMeNot中获取元素
            if(forgetMeNot != null) {
                lastRet = -1;
                lastRetElt = forgetMeNot.poll();
                if(lastRetElt != null) {
                    return lastRetElt;
                }
            }
            
            throw new NoSuchElementException();
        }
        
        // 移除刚刚遍历的那个元素
        public void remove() {
            if(expectedModCount != modCount) {
                throw new ConcurrentModificationException();
            }
            
            if(lastRet != -1) {
                // 移除上次遍历过的元素。如果小顶堆中的元素顺序发生了改变，则返回队尾元素
                E moved = PriorityQueue.this.removeAt(lastRet);
                
                lastRet = -1;
                
                /* 以下操作确保了被移除元素也能被遍历到 */
                
                // 如果队尾元素填充了移除位置
                if(moved == null) {
                    // 此时只需要递减游标，回到移除位置
                    cursor--;
                } else {
                    /*
                     * 如果小顶堆中的元素顺序发生了改变，
                     * 那么此时不清楚摘下的队尾结点去了哪里（当然，不是没办法知道），
                     * 此时需要利用forgetMeNot记录这些移除掉的队尾结点，以便稍后遍历
                     */
                    if(forgetMeNot == null) {
                        forgetMeNot = new ArrayDeque<>();
                    }
                    forgetMeNot.add(moved);
                }
            } else if(lastRetElt != null) {
                PriorityQueue.this.removeEq(lastRetElt);
                lastRetElt = null;
            } else {
                // 禁止在迭代器遍历队列之前就移除元素
                throw new IllegalStateException();
            }
            
            expectedModCount = modCount;
        }
    }
    final class PriorityQueueSpliterator implements Spliterator<E> {
        private int index;            // current index, modified on advance/split
        private int fence;            // -1 until first use
        private int expectedModCount; // initialized when fence set
        
        /** Creates new spliterator covering the given range. */
        PriorityQueueSpliterator(int origin, int fence, int expectedModCount) {
            this.index = origin;
            this.fence = fence;
            this.expectedModCount = expectedModCount;
        }
        
        // 从容器的指定范围切割一段元素，将其打包到Spliterator后返回
        public PriorityQueueSpliterator trySplit() {
            int hi = getFence(), lo = index, mid = (lo + hi) >>> 1;
            return (lo >= mid) ? null : new PriorityQueueSpliterator(lo, index = mid, expectedModCount);
        }
        
        // 遍历容器内每个元素，在其上执行相应的择取操作
        public void forEachRemaining(Consumer<? super E> action) {
            if(action == null) {
                throw new NullPointerException();
            }
            
            if(fence<0) {
                fence = size;
                expectedModCount = modCount;
            }
            
            final Object[] es = queue;
            int i, hi;
            E e;
            
            for(i = index, index = hi = fence; i<hi; i++) {
                if((e = (E) es[i]) == null) {
                    break;      // must be CME
                }
                action.accept(e);
            }
            
            if(modCount != expectedModCount) {
                throw new ConcurrentModificationException();
            }
        }
        
        // 对容器中的单个当前元素执行择取操作
        public boolean tryAdvance(Consumer<? super E> action) {
            if(action == null) {
                throw new NullPointerException();
            }
            
            if(fence<0) {
                fence = size;
                expectedModCount = modCount;
            }
            
            int i;
            if((i = index)<fence) {
                index = i + 1;
                
                E e;
                if((e = (E) queue[i]) == null || modCount != expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                
                action.accept(e);
                
                return true;
            }
            return false;
        }
        
        public long estimateSize() {
            return getFence() - index;
        }
        
        public int characteristics() {
            return Spliterator.SIZED | Spliterator.SUBSIZED | Spliterator.NONNULL;
        }
        
        private int getFence() { // initialize fence to size on first use
            int hi;
            if((hi = fence)<0) {
                expectedModCount = modCount;
                hi = fence = size;
            }
            return hi;
        }
    }
}

```