package spl.lae;
import java.io.IOException;
import java.text.ParseException;

import parser.*;

public class Main {
    public static void main(String[] args) {
      /// main
        LinearAlgebraEngine LAE = new LinearAlgebraEngine(4);
        InputParser IP = new InputParser();
        try {
            ComputationNode computationNode = IP.parse("example.json");
            ComputationNode Root = LAE.run(computationNode);
            OutputWriter.write(Root.getMatrix(), "out.json");
        } catch (Exception ignored) {}
    }
}