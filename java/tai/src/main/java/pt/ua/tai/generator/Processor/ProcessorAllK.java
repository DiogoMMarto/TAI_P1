package pt.ua.tai.generator.Processor;

import java.util.*;

public class ProcessorAllK extends Processor{
    private List<Map<String, Map<Character, Integer>>> subCountTables;
    public ProcessorAllK(String content, int k) {
        super(content, k);
        subCountTables=new ArrayList<>();
    }

    @Override
    public void process() {
        //prevent lost information in subtables for context.length<k between the 1 char in the content and the char at k position
        for (int subk = 1; subk < k; subk++) {
            for (int wp = 0; wp < k-subk-1; wp++) {
                String context = content.substring(wp,wp+subk);
                char c = content.charAt(wp+subk);

                Map<Character, Integer> tempSub = subCountTables.get(subk-1).getOrDefault(context, new HashMap<>());
                tempSub.put(c,tempSub.getOrDefault(c,0)+1);
                subCountTables.get(subk-1).put(context,tempSub);
            }
        }
        // same as processor for k, and fill the subtables with all info from the current suffix of size j
        for (int i = k; i < content.length(); i++) {
            String context = content.substring(i - k, i);

            ArrayList<String> subContexts =new ArrayList<>();
            for (int j = 1; j < k; j++) {
                subContexts.add(j-1,context.substring(k-j));
            }

            char c = content.charAt(i);

            Map<Character, Integer> temp = countTable.getOrDefault(context, new HashMap<>());
            temp.put(c, temp.getOrDefault(c, 0) + 1);
            countTable.put(context, temp);

            for (int j = 0; j < k-1; j++) {
                Map<Character, Integer> tempSub = subCountTables.get(j).getOrDefault(subContexts.get(j), new HashMap<>());
                tempSub.put(c,tempSub.getOrDefault(c,0)+1);
                subCountTables.get(j).put(subContexts.get(j),tempSub);
            }
        }
    }

    public List<Map<String, Map<Character, Integer>>> getSubCountTables(){
        return subCountTables;
    }
}
