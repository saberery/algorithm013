/*
思路1，将尾部k个数拿出存起来，再将前k-1个数向前移动，再将拿出的数存入数组。空间复杂度O(k)不符合题目要求
思路2，每次挪动一位，循环k次，空间复杂度O(1)，时间复杂度O(k*n)
思路3，反转法和最大公约数法
 */


class Solution {
    public void rotate(int[] nums, int k) {
        while(k != 0 ){
            nums = moveOneStep(nums);
            k--;
        }
    }

    public int[] moveOneStep(int[] nums){
        int length = nums.length;
        int temp = nums[length - 1];
        for (int i = length - 1; i > 0; i--){
            nums[i] = nums[i - 1];
        }
        nums[0] = temp;
        return nums;
    }

}