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
    public TreeNode lowestCommonAncestor(TreeNode root, TreeNode p, TreeNode q) {
        return findNode(root,p,q);
    }

    public TreeNode findNode (TreeNode cur, TreeNode p, TreeNode q){

        //遍历到底部就返回，递归结束条件
        if (cur == null || cur == p || cur == q) return cur;

        //递归
        TreeNode left = findNode(cur.left, p, q);
        TreeNode right = findNode(cur.right, p, q);

        if (left == null) return right;
        if (right == null) return left;
        return cur;

    }
}