import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

class Apriori {
  public static void main(String[] args) throws FileNotFoundException, IOException {
    // read command line arguments
    // first one is the input file and the second one is support value
    if (args.length != 2) {
      System.out.println("Usage: java Apriori <input file> <support value>");
      System.exit(1);
    }
    String inputFile = args[0];
    // create list of items. where item is a list of strings
    ArrayList<Set<String>> transactionList = new ArrayList<>();
    // read input file and populate items
    // read csv file at inputFile and populate items
    try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
      String line;
      while ((line = br.readLine()) != null) {
          String[] values = line.split(",");
          // add sorted items
          Set<String> item = new HashSet<>(Arrays.asList(values));
          transactionList.add(item);
      }
    }
    Integer support = (int) Math.ceil((Double.parseDouble(args[1]) * transactionList.size()));

    List<Item> singletons = frequentSingletons(transactionList, support);
    // print singletons
    List<List<Item>> frequentItems = new ArrayList<>();
    // empty list
    frequentItems.add(new ArrayList<>()); // 0
    frequentItems.add(singletons); // 1
    int k = 2;
    // print candidates
    while (frequentItems.get(k - 1).size() > 0) {
      List<List<String>> candidatesK = Apriori_gen(frequentItems.get(k - 1), k);
      // for in transactionList

      List<Item> itemsWithSupport = new ArrayList<>();

      for (List<String> candidate : candidatesK) {
        itemsWithSupport.add(new Item(candidate, 0));
      }

      for (Set<String> transaction : transactionList) {
        for (Item item : itemsWithSupport) {
          if (transaction.containsAll(item.itemsName)) {
            item.support++;
          }
        }
      }
      // print frequent items
      List<Item> f = new ArrayList<>();
      for (Item item : itemsWithSupport) {
        if (item.support >= support) {
          f.add(item);
        }
      }
      frequentItems.add(f);
      k++;
    }
    // print frequent items
    List<Item> f = new ArrayList<>();
    for (int i = 1; i < frequentItems.size(); i++) {
      for (Item item : frequentItems.get(i)) {
        f.add(item);
      }
    }
    // sort by support
    Collections.sort(f, (a, b) -> {
      return a.support - b.support;
    });
    for (Item item : f) {
      // comma separated
      for (int i = 0; i < item.itemsName.size(); i++) {
        System.out.print(item.itemsName.get(i));
        if (i < item.itemsName.size() - 1) {
          System.out.print(", ");
        }
      }
      //print 7 digits after decimal point
      System.out.println(" " + String.format("%.8f", (double) item.support / transactionList.size()));
    }
  }


  public static class Item {
    List<String> itemsName;
    int support;
    public Item(List<String> itemsName, int support) {
      this.itemsName = itemsName;
      this.support = support;
    }
  }

  private static List<Item> frequentSingletons(ArrayList<Set<String>> items, int support) {
    // count the frequency of each item
    List<String> uniqueItems = new ArrayList<>();
    for (Set<String> item : items) {
      for (String i : item) {
        if (!uniqueItems.contains(i)) {
          uniqueItems.add(i);
        }
      }
    }
    // list with support value
    List<Item> itemsWithSupport = new ArrayList<>();

    for (String i : uniqueItems) {
      int count = 0;
      for (Set<String> item : items) {
        if (item.contains(i)) {
          count++;
        }
      }
      if (count >= support) {
        itemsWithSupport.add(new Item(new ArrayList<String>(Arrays.asList(i)), count));
      }
    }
    return itemsWithSupport;
  }

  // Apriori generator
  public static List<List<String>> Apriori_gen(List<Item> items, int k) {
    // generate candidates
    Set<String> allItems = new HashSet<>();
    for (Item item : items) {
      allItems.addAll(item.itemsName);
    }
    // set -> list
    List<String> allItemsList = new ArrayList<>(allItems);
    Collections.sort(allItemsList);
    return new CandidateGen(items, k).genCandidates(allItemsList, new ArrayList<>(), 0);
  }

  // generate candidates
  public static class CandidateGen {

    private ArrayList<Set<String>> items;
    private int k;
    // constructor
    public CandidateGen(List<Item> items, int k) {
      ArrayList<Set<String>> res = new ArrayList<>();
      for (Item item : items) {
        res.add(new HashSet<>(item.itemsName));
      }
      this.items = res;
      this.k = k;
    }

    // generate candidates
    public List<List<String>> genCandidates(List<String> uniqueElements, List<String> cur, int pos) {
      if (pos == uniqueElements.size()) {
        if (cur.size() != k) return new ArrayList<>();
        // check if all subsets length k - 1 of cur are in items
        for (int i = 0; i < cur.size(); i++) {
          List<String> subset = new ArrayList<>(cur);
          subset.remove(i);
          Boolean good = false;
          for (Set<String> item : items) {
            if (item.containsAll(subset)) {
              good = true;
              break;
            }
          }
          if (!good) {
            return new ArrayList<>();
          }
        }
        List<List<String>> candidates = new ArrayList<>();
        candidates.add(cur);
        return candidates;
      }
      List<List<String>> list1 = genCandidates(uniqueElements, cur, pos + 1);
      List<String> newCur = new ArrayList<>(cur);
      newCur.add(uniqueElements.get(pos));
      List<List<String>> list2 = genCandidates(uniqueElements, newCur, pos + 1);

      List<List<String>> res = new ArrayList<>();
      for (List<String> l : list1) {
        if (l.size() > 0)
          res.add(l);
      }
      for (List<String> l : list2) {
        if (l.size() > 0)
          res.add(l);
      }
      return res;
    }
  }
}
