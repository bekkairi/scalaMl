package binaryGap.binaryGap;


import java.util.Hashtable;
import java.util.Map;

public class CountNonDivisible {

    public static void main(String... args) {
        // crete a table and add some values
        Map<String, Integer>
                table = new Hashtable<>();

        table.put("Pen", 10);
        table.put("Book", 500);
        table.put("Clothes", 400);
        table.put("Mobile", 5000);

        // print map details
        System.out.println("hashTable: "
                + table.toString());

        // Inserting non-existing key with value
        // using putIfAbsent method
        String retValue
                = String.valueOf(table
                .putIfAbsent("Booklet", 2500));

        // Print the returned value
        System.out.println("Returned value "
                + "for Key 'Booklet' is: "
                + retValue);

        // print new mapping
        System.out.println("hashTable: "
                + table);

        // Inserting existing key with value
        // using putIfAbsent method
        retValue
                = String.valueOf(table
                .putIfAbsent("Book", 4500));

        // Print the returned value
        System.out.println("Returned value"
                + " for key 'Book' is: "
                + retValue);

        // print new mapping
        System.out.println("hashTable: "
                + table);

    }

    class test implements Comparable {


        @Override
        public int compareTo(Object o) {
            return 0;
        }
    }
}
