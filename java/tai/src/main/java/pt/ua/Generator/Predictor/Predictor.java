package pt.ua.Generator.Predictor;

import pt.ua.Generator.Predictor.ContextSearcher.ContextSearcher;

import java.util.Map;

public abstract class Predictor {
    protected ContextSearcher contextSearcher;
    protected String response;
    protected String context;
    protected Map<Character,Integer> entries;
    protected Map<String, Map<Character,Integer>> table;
    protected int responseSize;
    protected int k;

    public Predictor(ContextSearcher contextSearcher,int responseSize, int k) {
        this.contextSearcher=contextSearcher;
        this.responseSize=responseSize;
        this.k=k;
    }

    public void setTable(Map<String, Map<Character, Integer>> table) {
        this.table = table;
    }

    public void feedPrior(String prior){
        this.response=prior;
    }

    public String generateResponse(){
        for (int i = 0; i < responseSize; i++) {
            response+=generateChar();
        }
        return response;
    }

    public void generateResponseToCmd(){
        char c;
        for (int i = 0; i < responseSize; i++) {
            c=generateChar();
            response+=c;
            System.out.print(c);
        }
    }

    public char generateChar(){
        String tempContext=response.substring(response.length() - k);
        try {
            context=contextSearcher.getFittingContext(tempContext);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ' ';
        }

        entries=getEntries(context);

        return chooseChar();
    }

    protected Map<Character, Integer> getEntries(String context) {
        return table.get(context);
    }


    protected abstract char chooseChar();
}
