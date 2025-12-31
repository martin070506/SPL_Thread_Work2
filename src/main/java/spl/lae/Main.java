package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws  IOException {
        // main
        int threads = Integer.parseInt(args[0]);
        String inputFilePath = args[1];
        String outputFilePath = args[2];

        try {
            LinearAlgebraEngine LAE = new LinearAlgebraEngine(threads);
            InputParser IP = new InputParser();
            ComputationNode computationNode = IP.parse(inputFilePath);
            ComputationNode Root = LAE.run(computationNode);
            OutputWriter.write(Root.getMatrix(), outputFilePath);
        } catch (Exception e){
            OutputWriter.write(e.getMessage(),outputFilePath);
            System.exit(1);
        }
    }
}
