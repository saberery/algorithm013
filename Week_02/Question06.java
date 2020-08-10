package week02;

public class Week02Question06 {

    public static void main(String[] args) {

    }

    //思路1：将列表中的字符串取出，每一个排序一次，添加到hashmap中，不存在则新建一个list，存在则添加到原来的list中

    class Solution {
        public List<List<String>> groupAnagrams(String[] strs) {
            List<List<String>> resList = new ArrayList<>();
            Map<String,List<String>> map = new HashMap<>();

            for (int i = 0; i < strs.length; i++){
                char[] ca = strs[i].toCharArray();
                Arrays.sort(ca);
                String sortedca = String.valueOf(ca);
                if (! map.containsKey(sortedca)){
                    List<String> res = new ArrayList<>();
                    res.add(strs[i]);
                    map.put(sortedca, res);
                }else{
                    map.get(sortedca).add(strs[i]);
                }
            }

            for (String key: map.keySet()){
                resList.add(map.get(key));
            }

            return resList;

        }
    }

}
