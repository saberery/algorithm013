package week02;

import java.util.HashMap;
import java.util.Map;

public class Week03Question03 {

    public static void main(String[] args) {

    }

    //思路2：将target-数组的值存入hashmap中

    class Solution {
        public int[] twoSum(int[] nums, int target) {
            Map<Integer, Integer> map = new HashMap<Integer, Integer>();
            for (int i = 0; i < nums.length; i++){
                map.put(target-nums[i],i);
            }
            for (int j = 0; j < nums.length; j++){
                if (map.containsKey(nums[j]) && map.get(nums[j]) != j){
                    int a []  = {map.get(nums[j]),j};
                    return a;
                }
            }
            return null;
        }

    }

}
