import java.util.*;

public class Test {
    public static void main(String[] args) {
        Test test = new Test();
        List<String> ans = test.letterCasePermutation("a1b2");
        for (String s : ans) {
            System.out.println(s);
        }
    }

    List<String> ans = new ArrayList<>();
    char[] path;

    public List<String> letterCasePermutation(String S) {
        if (S.length() == 0) return ans;

        // path = new char[S.length()];
        // backtrack2(S, 0);
        path = S.toCharArray();
        backtrack(0);
        return ans;
    }

    public void backtrack(int curIndex) {
        // if (curIndex == path.length) {
        //     ans.add(new String(path));
        //     return;
        // }

        for (int i = curIndex; i < path.length; i++) {
            if (Character.isLetter(path[i])) {
                path[i] = (char) (path[i] ^ 1 << 5);
                backtrack(i + 1);
                path[i] = (char) (path[i] ^ 1 << 5);
            }
        }

        ans.add(new String(path));
    }


    public void backtrack2(String S, int curIndex) {
        if (curIndex == S.length()) {
            ans.add(new String(path));
            return;
        }
        // TODO:
        //  1.Ϊʲô���ﲻ��for����
        //  2.ע������������ѡ�񣬶�����һ��ѡ��

        // ��һ��
        path[curIndex] = S.charAt(curIndex);
        backtrack2(S, curIndex + 1);

        // �ڶ���
        if (Character.isLetter(S.charAt(curIndex))) {
            path[curIndex] = (char) (path[curIndex] ^ 1 << 5);
            backtrack2(S, curIndex + 1);
        }
    }
}
