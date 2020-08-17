/**
 * Definition for a binary tree node.
 * public class TreeNode {
 *     int val;
 *     TreeNode left;
 *     TreeNode right;
 *     TreeNode(int x) { val = x; }
 * }
 */
class Solution {
    public TreeNode buildTree(int[] preorder, int[] inorder) {
        return makeTree(preorder,0,preorder.length, inorder,0,inorder.length);
    }

    public TreeNode makeTree(int[] preorder, int p_start, int p_length, int[] inorder, int i_start, int i_length){
        //递归结束条件
        if (p_start == p_length) return null;

        //处理当前层逻辑
        //将根节点加入TreeNode
        TreeNode treeNode = new TreeNode(preorder[p_start]);
        //找到传进来中序遍历数组中根节点的位置
        int inorder_index = 0; //inorder_index - 1 传给生成左子树的函数 && inorder_index到i_length的部分传给生成右子树的函数
        for (int i = i_start; i < i_length; i++){
            if (inorder[i] == preorder[p_start]){
                inorder_index = i;
                break;
            }
        }
        //leftNum
        int leftNum = inorder_index - i_start;
        //向下递归生成左子树
        treeNode.left = makeTree(preorder, p_start + 1, p_start + leftNum + 1, inorder, i_start, inorder_index);
        //向下递归生成右子树
        treeNode.right = makeTree(preorder, p_start + leftNum + 1, p_length, inorder, inorder_index + 1, i_length);

        return treeNode;

    }
}