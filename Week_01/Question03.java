/* 删除排序数组中的重复项
思路：双指针，一个指针遍历，一个指针从0位置开始遇到非重复项+1插入非重复元素
 */

import org.w3c.dom.ls.LSOutput;

public class Week01Question02 {

    public static void main(String[] args) {

    }


}

class Solution {
    public int removeDuplicates(int[] nums) {
        if (nums.length == 0) return 0;
        int count = 1;
        int j = 0;
        for (int i = 0; i < nums.length - 1; i++) {
            if (nums[i] == nums[i + 1]) {
                continue;
            }else{
                j++;
                nums[j] = nums[i + 1];
                count++;
            }
        }
        return count;
    }
}
