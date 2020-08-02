/*
 * 用 add first 或 add last 这套新的 API 改写 Deque 的代码
 * */

import java.util.Deque;
import java.util.LinkedList;

public class Question01 {

    public static void main(String[] args) {

        Deque<String> deque = new LinkedList<String>();

        deque.addFirst("a");
        deque.addLast("c");
        deque.addLast("b");
        System.out.println(deque);

        String str = deque.peek();
        System.out.println(str);
        System.out.println(deque);

        while(deque.size()>0){
            System.out.println(deque.pop());
        }
        System.out.println(deque);


    }

}

