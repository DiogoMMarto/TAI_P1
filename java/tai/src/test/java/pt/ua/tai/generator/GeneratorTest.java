package pt.ua.tai.generator;

import org.junit.jupiter.api.Test;
import pt.ua.tai.generator.Enum.Mode;
import pt.ua.tai.generator.Enum.Search;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import static pt.ua.tai.utils.FileUtil.readFile;

class GeneratorTest {
    int responseLength = 10000;
    int priorSamplePos = 50;
    int seed = 85095;

    @Test
    void findIdealK() {
        String mode = "PROBABILITY";
        String searchMode = "CUTFIRSTCHAR";
        String output = "";

        for (int s = 1; s < 6; s++) {
            String fileName = "sequence" + s + ".txt";
            String source = readFile(fileName);

            for (int k = 1; k < 11; k++) {
                output += "----------------k=" + k + "---------file=" + s + "--------------\n";

                String prior = source.substring(priorSamplePos, priorSamplePos + k);


                long startTime = System.nanoTime();
                //----------
                Generator generator = new Generator(prior, responseLength, fileName);
                try {
                    generator.setK(k);
                    generator.setMode(mode);
                    generator.setSearchMode(searchMode);
                    generator.seeding(seed);
                    generator.priorFixing();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                generator.init();

                String response = generator.generate();
                //----------------
                long endTime = System.nanoTime();
                long duration = endTime - startTime; // Time in nanoseconds
                output += response + "\n\n";
                output += "t= " + duration + "\n";

            }
        }

        try {
            Files.write(Paths.get("resultsk.txt"), output.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void findIdealAlpha() {
        int responseLength = 1000;
        String mode = "PROBABILITYALPHA";
        String searchMode = "CUTFIRSTCHAR";

        String output = "";

        double[] alphas = {0.004096, 0.01024, 0.0256, 0.064, 0.16, 0.4};
        int k = 6;//ToDo: change based on test "findIdealK()"

        int[] aux={2,5};
        //for (int s = 1; s < 6; s++) {
        for(int s:aux){
            String fileName = "sequence" + s + ".txt";
            String source = readFile(fileName);

            for (double alpha : alphas) {
                System.out.println("----------------alpha=" + alpha + "---------file=" + s + "--------------\n");
                output += "----------------alpha=" + alpha + "---------file=" + s + "--------------\n";
                String prior = source.substring(priorSamplePos, priorSamplePos + k);


                long startTime = System.nanoTime();
                //----------
                Generator generator = new Generator(prior, responseLength, fileName);
                try {
                    generator.setK(k);
                    generator.setAlpha(alpha);
                    generator.setMode(mode);
                    generator.setSearchMode(searchMode);
                    generator.seeding(seed);
                    generator.priorFixing();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                generator.init();

                String response = generator.generate();
                //----------------
                long endTime = System.nanoTime();
                long duration = endTime - startTime; // Time in nanoseconds
                output += response + "\n\n";
                output += "t= " + duration + "\n";
            }
        }

        try {
            Files.write(Paths.get("resultsalpha.txt"), output.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void compareModes() {
        String searchMode = "CUTFIRSTCHAR";

        String output = "";

        double alpha = 0.004096;// //ToDo: change based on test "findIdealAlpha()"
        int k = 6;//ToDo: change based on test "findIdealK()"


        int[] aux={2,5};
        //for (int s = 1; s < 6; s++) {
        for(int s:aux){
            String fileName = "sequence" + s + ".txt";
            String source = readFile(fileName);
            for (Mode mode : Mode.values()) {
                output += "----------------mode=" + mode.toString() + "---------file=" + s + "--------------\n";
                String prior = source.substring(priorSamplePos, priorSamplePos + k);


                long startTime = System.nanoTime();
                //----------
                Generator generator = new Generator(prior, responseLength, fileName);
                try {
                    generator.setK(k);
                    generator.setAlpha(alpha);
                    generator.setMode(mode.toString());
                    generator.setSearchMode(searchMode);
                    generator.seeding(seed);
                    generator.priorFixing();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                generator.init();

                String response = generator.generate();
                //----------------
                long endTime = System.nanoTime();
                long duration = endTime - startTime; // Time in nanoseconds
                output+=response+"\n\n";
                output+="t= "+duration+"\n";
            }
        }

        try {
            Files.write(Paths.get("resultsmode.txt"), output.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void compareSearchModes() {
        String output = "";

        double alpha = 0.004096;// //ToDo: change based on test "findIdealAlpha()"
        int k = 6;//ToDo: change based on test "findIdealK()"
        String mode = "PROBABILITY";//ToDo: change based on test "compareModes()"


        int[] aux={2,5};
        //for (int s = 1; s < 6; s++) {
        for(int s:aux){
            String fileName = "sequence" + s + ".txt";
            String source = readFile(fileName);

            for (Search searchMode : Search.values()) {
                output += "----------------searchMode=" + searchMode.toString() + "---------file=" + s + "--------------\n";
                String prior = source.substring(priorSamplePos, priorSamplePos + k);


                long startTime = System.nanoTime();
                //----------
                Generator generator = new Generator(prior, responseLength, fileName);
                try {
                    generator.setK(k);
                    generator.setAlpha(alpha);
                    generator.setMode(mode);
                    generator.setSearchMode(searchMode.toString());
                    generator.seeding(seed);
                    generator.priorFixing();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                generator.init();

                String response = generator.generate();
                //----------------
                long endTime = System.nanoTime();
                long duration = endTime - startTime; // Time in nanoseconds
                output+=response+"\n\n";
                output+="t= "+duration+"\n";
            }
        }

        try {
            Files.write(Paths.get("resultsSearch.txt"), output.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}