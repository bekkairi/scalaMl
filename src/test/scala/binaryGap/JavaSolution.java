package binaryGap;

import java.util.ArrayList;

public class JavaSolution {

    public static void main(String... args) {
        String input = "abababa";
        String prefix;
        int product;
        int maxProduct = 0;
        for (int i = 1; i <= input.length(); i++) {
            prefix = input.substring(0, i);
            String substr;
            int occurs = 0;
            for (int j = prefix.length(); j <= input.length(); j++) {
                substr = input.substring(0, j);
                if (substr.endsWith(prefix))
                    occurs++;
            }
            product = occurs * prefix.length();
            System.out.println("product of " + prefix + " = " +
                    prefix.length() + " * " + occurs + " = " + product);
            maxProduct = (product > maxProduct) ? product : maxProduct;
        }
        System.out.println("maxProduct = " + maxProduct);

    }

    public static int solution(int[] A) {
        ArrayList<Integer> array = new ArrayList<Integer>();
        for (int i = 1; i < A.length - 1; i++) {
            if (A[i - 1] < A[i] && A[i + 1] < A[i]) {
                array.add(i);
            }
        }
        if (array.size() == 1 || array.size() == 0) {
            return array.size();
        }
        int sf = 1;
        int ef = array.size();
        int result = 1;
        while (sf <= ef) {
            int flag = (sf + ef) / 2;
            boolean suc = false;
            int used = 0;
            int mark = array.get(0);
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i) >= mark) {
                    used++;
                    mark = array.get(i) + flag;
                    if (used == flag) {
                        suc = true;
                        break;
                    }
                }
            }
            if (suc) {
                result = flag;
                sf = flag + 1;
            } else {
                ef = flag - 1;
            }
        }
        return result;
    }


}
