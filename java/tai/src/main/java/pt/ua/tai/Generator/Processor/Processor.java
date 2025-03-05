package pt.ua.tai.Generator.Processor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Processor {
    private int k;
    protected Map<String, Map<Character, Integer>> countTable;
    private String content;
    private Set<Character> alphabet;

    public Processor( String content, int k) {
        this.k = k;
        this.content = content;
        countTable=new HashMap<>();
    }

    public void process(){
        for (int i = k; i < content.length(); i++) {
            String context = content.substring(i - k, i);
            char c = content.charAt(i);

            Map<Character, Integer> temp = countTable.getOrDefault(context, new HashMap<>());
            temp.put(c, temp.getOrDefault(c, 0) + 1);
            countTable.put(context, temp);
        }
    }

    public Map<String,Map<Character,Integer>> getCountTable(){
        return this.countTable;
    }

    public Set<Character> getAlphabet(){
        if(alphabet==null){
            alphabet = new HashSet<>();
            for (char c : content.toCharArray()) {
                alphabet.add(c);
            }
        }
        return alphabet;
    }

}
