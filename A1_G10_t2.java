import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class FPGrowth {

  public static List<List<String>> readCSV(String filePath) {
    List<List<String>> data = new ArrayList<>();

    try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = br.readLine()) != null) {
        String[] items = line.split(",");
        List<String> transaction = new ArrayList<>();
        for (String item : items) {
          transaction.add(item.trim());
        }
        data.add(transaction);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return data;
  }

  public static List<String> createFrequencyTable(List<List<String>> transactions, double minSupport) {
    Map<String, Integer> itemFrequency = new HashMap<>();

    // Count item occurrences in all transactions
    for (List<String> transaction : transactions) {
        for (String item : transaction) {
            itemFrequency.put(item, itemFrequency.getOrDefault(item, 0) + 1);
        }
    }


    /* 
    // Print frequencies of all items before filtering
    System.out.println("Frequencies of all items before filtering:");
    for (Map.Entry<String, Integer> entry : itemFrequency.entrySet()) {
        System.out.println(entry.getKey() + " : " + entry.getValue());
    }

    */

    // Sort items in descending order of frequency
    List<Map.Entry<String, Integer>> sortedEntries = new ArrayList<>(itemFrequency.entrySet());
    sortedEntries.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

    // Filter items based on minimum support
    List<String> frequentItems = sortedEntries.stream()
            .filter(entry -> entry.getValue() >= minSupport * transactions.size())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());


    /* 
    // Print frequencies of frequent items after filtering
    System.out.println("Frequencies of frequent items after filtering:");
    for (String item : frequentItems) {
        System.out.println(item + " : " + itemFrequency.get(item));
    }
    */
    for (String item : frequentItems) {
        double support = (double) itemFrequency.get(item) / transactions.size();
        System.out.println(item + " : " + support);
    }
    return frequentItems;
}


  // Function to arrange items in each transaction based on the frequency table
  public static List<List<String>> arrangeItems(List<List<String>> transactions, List<String> frequencyTable) {
    List<List<String>> arrangedTransactions = new ArrayList<>();
    if (frequencyTable == null || frequencyTable.isEmpty() || transactions == null) {
        return arrangedTransactions; // Return empty list if any parameter is null or empty
    }

    // Create a map to store the frequency of each item
    Map<String, Integer> frequencyMap = new HashMap<>();
    for (int i = 0; i < frequencyTable.size(); i++) {
        frequencyMap.put(frequencyTable.get(i), frequencyTable.size() - i);
    }

    for (List<String> transaction : transactions) {
        List<String> arrangedTransaction = new ArrayList<>();
        // Only include items present in the frequency table
        for (String item : transaction) {
            if (frequencyTable.contains(item)) {
                arrangedTransaction.add(item);
            }
        }

        // Sort the transaction based on frequency and then alphabetically
        arrangedTransaction.sort((item1, item2) -> {
            int freqCompare = Integer.compare(frequencyMap.get(item2), frequencyMap.get(item1));
            return freqCompare != 0 ? freqCompare : item1.compareTo(item2);
        });

        arrangedTransactions.add(arrangedTransaction);
    }

    return arrangedTransactions;
  }


  private static class FPNode {
      private String itemName; // Name of the item
      private int count; // Count of occurrences of the item
      private FPNode parent; // Reference to the parent node
      private Map<String, FPNode> children; // Map to store children nodes

      // Constructor
      public FPNode(String itemName, int count, FPNode parent) {
          this.itemName = itemName;
          this.count = count;
          this.parent = parent;
          this.children = new HashMap<>();
      }

      // Getter methods
      public String getItemName() {
          return itemName;
      }

      public int getCount() {
          return count;
      }

      public FPNode getParent() {
          return parent;
      }

      // Method to add a child node
      public void addChild(String itemName, int count) {
          children.put(itemName, new FPNode(itemName, count, this));
      }

// Method to get a child node by item name
      public FPNode getChild(String itemName) {
          return children.get(itemName);
      }

      // Method to check if the node has a child with a given item name
      public boolean hasChild(String itemName) {
          return children.containsKey(itemName);
      }

          // Getter method to get children nodes
      public Map<String, FPNode> getChildren() {
        return children;
  }
  }

    // Class for FP Tree
  private static class FPTree {
    private FPNode root; // Root node of the FP tree
    private Map<String, FPNode> headerTable; // Header table to store references to the first node of each item

    // Constructor
    public FPTree() {
        this.root = new FPNode(null, 0, null); // Root node with null item name and count 0
        this.headerTable = new HashMap<>();
    }

    // Method to get the root node of the FP tree
    public FPNode getRoot() {
        return root;
    }

    // Method to add a transaction to the FP tree
    public void addTransaction(List<String> transaction, int count) {
        FPNode currentNode = root;

        // Traverse the tree based on the items in the transaction
        for (String item : transaction) {
            // Check if the current node has a child with the current item
            if (currentNode.hasChild(item)) {
                // If yes, increment the count of the child node
                currentNode.getChild(item).count += count;
            } else {
                // If no, add a new child node for the current item
                currentNode.addChild(item, count);
                // Update the header table to point to the new child node
                if (!headerTable.containsKey(item)) {
                    headerTable.put(item, currentNode.getChild(item));
                }
            }
            // Move to the child node corresponding to the current item
            currentNode = currentNode.getChild(item);
        }
    }

    // Method to check if the FP tree is empty
    public boolean isEmpty() {
        return root == null || root.getChildren().isEmpty();
    }

    // Method to recursively mine frequent itemsets from the FP tree
    public Map<List<String>, Integer> mineFrequentItemsets(FPNode node, List<String> prefix, double minSupport) {
        Map<List<String>, Integer> frequentItemsets = new HashMap<>();

        // Iterate over the node's children
        for (FPNode child : node.getChildren().values()) {
            // Create a new itemset by adding the child item to the prefix
            List<String> itemset = new ArrayList<>(prefix);
            itemset.add(child.getItemName());

            // Check if the support of the itemset meets the minimum support threshold
            if (child.getCount() >= minSupport) {
                // Add the frequent itemset to the map of frequent itemsets along with its support count
                frequentItemsets.put(itemset, child.getCount());

                // Recursively mine frequent itemsets from the child node
                Map<List<String>, Integer> childFrequentItemsets = mineFrequentItemsets(child, itemset, minSupport);
                // Add the frequent itemsets from the child node to the overall map
                frequentItemsets.putAll(childFrequentItemsets);
            }
        }

        return frequentItemsets;
    }
  }


    public static void main(String[] args) {

        // Check if the correct number of command-line arguments are provided
        if (args.length != 2) {
            System.err.println("Usage: java Main <input_file_path> <minsup>");
            System.exit(1);
        }

        // Retrieve input file path and minsup from command-line arguments
        String filePath = args[0];
        double minSupport = Double.parseDouble(args[1]);

        // Read CSV file and convert it into a list of lists
        List<List<String>> transactions = FPGrowth.readCSV(filePath);

        // Example usage of createFrequencyTable method
        List<String> frequencyTable = FPGrowth.createFrequencyTable(transactions, minSupport);


        // Example usage of arrangeItems method
        List<List<String>> arrangedTransactions = arrangeItems(transactions, frequencyTable);
        //System.out.println("Arranged Transactions: " + arrangedTransactions);

        
        // Create the FP tree
        FPTree fpTree = new FPTree();
        for (List<String> transaction : arrangedTransactions) {
            fpTree.addTransaction(transaction, 1);
        }
        /* 
        // Mine frequent itemsets from the FP tree
        Map<List<String>, Integer> frequentItemsetsWithSupport = fpTree.mineFrequentItemsets(fpTree.getRoot(), new ArrayList<>(), minSupport);

        
        // Output frequent itemsets along with their support values
        
        System.out.println("\nFrequent Itemsets with Support:");
        for (Map.Entry<List<String>, Integer> entry : frequentItemsetsWithSupport.entrySet()) {
            // Calculate support value for each item
            double support = (double) entry.getValue() / transactions.size();
            //System.out.println(entry.getKey() + " : " + entry.getValue());
            // Print only if the support value is greater than the minimum support threshold
            if (support >= minSupport) {
                System.out.println(entry.getKey() + " : " + support);
            }
        }
        */
}   

}