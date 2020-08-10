package week02;

import java.util.HashMap;
import java.util.Map;

public class Week02Question02 {

    public static void main(String[] args) {

    }

    //思路1：将两个字符串转换成数组，将数组排序之后在比较
    //时间复杂度：两遍排序算法；空间复杂度：排序算法所用到的空间复杂度
    //思路2：遍历一个字符串，将字母存入哈希表中，遍历第二个字符串，从哈希表中扣除数据
    //时间复杂度：O(2n)；空间复杂度：O(n)

    class Solution {
        public boolean isAnagram(String s, String t) {
            char [] char_s = s.toCharArray();
            char [] char_t = t.toCharArray();
            Map<String, Integer> map = new HashMap<String, Integer>();
            for (int i = 0; i < char_t.length; i++){
                if (map.containsKey(String.valueOf(char_t[i]))){
                    int count = map.get(String.valueOf(char_t[i]));
                    map.put(String.valueOf(char_t[i]),++count);
                }else{
                    map.put(String.valueOf(char_t[i]),1);
                }
            }
            for (int j = 0; j < char_s.length; j++){
                if (map.containsKey(String.valueOf(char_s[j]))){
                    int count = map.get(String.valueOf(char_s[j]));
                    map.put(String.valueOf(char_s[j]),--count);
                }else{
                    return false;
                }
            }
            for (int value: map.values()){
                if (value != 0) return false;
            }

            return true;

        }
    }

}
