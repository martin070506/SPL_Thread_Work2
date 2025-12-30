package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) throws ParseException, IOException {
//      /// main
        try {
            LinearAlgebraEngine LAE = new LinearAlgebraEngine(6);
            InputParser IP = new InputParser();
            ComputationNode computationNode = IP.parse("example.json");
            ComputationNode Root = LAE.run(computationNode);
            OutputWriter.write(Root.getMatrix(), "out.json");
        } catch (Exception e){
            OutputWriter.write(e.getMessage(), "out.json");
        }
    }
}
