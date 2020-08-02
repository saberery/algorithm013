/*思路1，双指针暴力求解
* 思路2，target - 数组值存入hashmap中，再遍历hashmap
* */

class Solution {
    public int[] twoSum(int[] nums, int target) {
        int length = nums.length;

        for (int i = 0; i < length - 1; i ++){
            for (int j = i + 1; j < length; j++)
                if ((nums[i] + nums[j]) == target){
                    int a [] = {i,j};
                    return a;
                }
        }
        return null;
    }
}