# hashmap总结
hashmap继承了AbstractMap类，实现了Map接口。
主要方法：containsKey​,containsValue​,get,isEmpty,put,remove​,size

构造函数中有一个loadFactor用来调整map的大小，小于loadFactor*capacity则会重新建立hashtable

hashtable是一个数组加上链表的形式，Node<K,V>[] tab声明了数组空间，每次在数组中存放一个node，如果重复地址会通过链表的形式在node后增加新的node地址