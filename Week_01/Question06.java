/*给你两个有序整数数组 nums1 和 nums2，请你将 nums2 合并到 nums1 中，使 nums1 成为一个有序数组。
 * 思路1，直接将num2添加到num1中，使用排序算法解决
 * 思路2，从尾部开始比较，设置三指针，i -> nums1, j -> nums2, k -> m + n -1; nums1 >= nums2, nums[k--] = nums1[i--] ;
 * nums2 > nums1, nums1[k--] = nums2[j--]，剩下的一定是nums2中有值，再遍历nums2中的值添加到nums1。注意边界值的测试。
 * */

public class Week01Question06 {

    public static void main(String[] args) {

    }

}

class Question06Solution {
    public void merge(int[] nums1, int m, int[] nums2, int n) {
        int i = m - 1;
        int j = n - 1;
        int k = n + m - 1;
        while (i != -1 && j != -1) {
            if (nums2[j] >= nums1[i]) {
                nums1[k--] = nums2[j--];
            } else {
                nums1[k--] = nums1[i--];
            }
        }
        if (j != -1) {
            for (int z = 0; z <= j; z++) {
                nums1[z] = nums2[z];
            }
        }
    }
}
