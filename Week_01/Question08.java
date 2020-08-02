class Solution {
    public void moveZeroes(int[] nums) {
        //建立两个指针，一个记录0的数量，一个用来重新填充数组
        int sum = 0;
        int position = 0;
        int length = nums.length;
        for (int i = 0; i < length; i++){
            if (nums[i] == 0){
                sum++;
            }else{
                nums[position] = nums[i];
                position++;
            }
        }
        for (int j = 0; j < sum; j++) {
            nums[position+j] = 0;
        }
    }
}