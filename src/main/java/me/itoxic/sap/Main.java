package me.itoxic.sap;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import me.itoxic.sap.base.Instruction;
import me.itoxic.sap.base.MemoryValue;
import me.itoxic.sap.base.Operator;
import me.itoxic.sap.base.Output;

import java.io.*;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Scanner;

public class Main extends Application {

    private static Stage stage;
    private static Main instance;

    public static void main(String[] args) {

        Application.launch(Main.class);

    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        instance = this;
        stage = primaryStage;

        stage.setTitle("Simple As Possible Computer - Generador de Programas [v2020-01]");
        stage.setResizable(false);
        stage.initStyle(StageStyle.DECORATED);

        stage.getIcons().add(new Image(getResourceAsStream("icon.png")));
        stage.setIconified(true);

        stage.setOnCloseRequest(e -> {
            System.exit(-1);
        });

        Parent root = FXMLLoader.load(getClass().getResource("pgen.fxml"));
        Scene scene = new Scene(root);

        primaryStage.setScene(scene);
        primaryStage.show();

    }

    public static Output process(String operation) {

        LinkedList<Instruction> instructions = new LinkedList<>();
        LinkedList<MemoryValue> memories = new LinkedList<>();

        char[] characters = operation.toCharArray();

        String lastMemory = "";
        String lastOperator = "";

        int insPos = 0;

        A : for(int i = 0; i < operation.length(); i++) {

            char current = characters[i];

            if(insPos == 0 && Operator.check(current) != null) {

                memories.add(new MemoryValue(lastMemory));

                lastOperator = current + "";
                lastMemory = "";

                insPos = 1;
                continue A;

            }

            if(current == ' ')
                continue A;

            Operator operator = Operator.check(current);

            if(operator == null) {

                lastMemory += current;
                continue A;

            } else {

                if(!lastMemory.isEmpty() && !lastOperator.isEmpty()) {

                    Operator lOperator = Operator.check(lastOperator.charAt(0));

                    memories.add(new MemoryValue(lastMemory));
                    instructions.add(new Instruction(lOperator.getInstruction().getLiteral(), lastMemory));

                }

                lastMemory = "";
                lastOperator = current + "";
                continue A;

            }

        }

        if(!lastMemory.isEmpty() && !lastOperator.isEmpty()) {

            memories.add(new MemoryValue(lastMemory));
            instructions.add(new Instruction(Operator.check(lastOperator.charAt(0)).getInstruction().getLiteral(), lastMemory));

        }

        if(instructions.size() > memories.size())
            throw new RuntimeException("Deben haber más numeros que operaciones.");

        return createOutput(instructions, memories);

    }

    private static Output createOutput(LinkedList<Instruction> pins, LinkedList<MemoryValue> mem) {

        final LinkedList<MemoryValue> output_mem = new LinkedList<>();
        final LinkedList<Instruction> output_ins = new LinkedList<>();
        final int base = 3;

        int baseMem = base + pins.size();
        int memPos = baseMem;

        for(MemoryValue memoryValue : mem)
            if(!containsMemory(memoryValue.getOutput(), output_mem)) {
                String def = memoryValue.getValue();
                output_mem.add(new MemoryValue(def).setPosition(MemoryValue.MemoryPosition.getByPosition(memPos++)));
            }

        output_ins.add(new Instruction(Static.LDA.getLiteral(), output_mem.get(0).getPosition().getLiteral()));

        for(Instruction instruction : pins)
            output_ins.add(new Instruction(instruction.getInstructionValue(), getMemoryPosition(instruction.getMemoryValue(), output_mem).getLiteral()));

        output_ins.add(new Instruction(Static.OUT.getLiteral(), "0"));
        output_ins.add(new Instruction(Static.HLT.getLiteral(), "0"));

        return sendProcess(output_mem, output_ins);

    }

    private static Output sendProcess(LinkedList<MemoryValue> memoryValues, LinkedList<Instruction> instructions) {

        Scanner scanner = null;
        StringBuffer buffer = new StringBuffer();

        Iterator<Instruction> insIterator = instructions.iterator();
        Iterator<MemoryValue> memIterator = memoryValues.iterator();

        scanner = new Scanner(getResourceAsStream("template.hex"));

        while(scanner.hasNextLine()) {
            buffer.append(scanner.nextLine() + System.lineSeparator());
        }

        String content = buffer.toString();
        LinkedList<String> steps = new LinkedList<>();

        for(int i = 1; i <= 16; i++) {

            if(insIterator.hasNext()) {
                Instruction last = insIterator.next();
                content = content.replaceFirst("%" + i, last.getOutput());
                steps.add(last.getLiteralInstruction() + (last.getMemoryValue() == "0" ? "" : " R" + last.getMemoryValue()));
                continue;
            }

            if(memIterator.hasNext()) {
                MemoryValue last = memIterator.next();
                content = content.replaceFirst("%" + i, last.getOutput());
                steps.add("R" + last.getPosition().getLiteral() + " " + last.getValue());
                continue;
            }

            content = content.replaceFirst("%" + i, "00");

        }

        String[] lines = content.split(System.lineSeparator());
        StringBuffer output = new StringBuffer();

        for(String line : lines)
            if(line.length() > 39) {

                String data = line.substring(1, 41);
                output.append(":" + data + calculate_checksum8(data) + System.lineSeparator());

            } else {

                output.append(line);

            }
        return new Output(instructions, memoryValues).setFile(output.toString()).setOutput(steps);

    }

    private static String calculate_checksum8(String N) {

        String strN = N;
        strN = strN.toUpperCase();

        String strHex = "0123456789ABCDEF";
        String strResult = "";

        int result = 0;
        int fctr = 16;

        for (int i = 0; i < strN.length(); i++) {

            if (strN.charAt(i) == ' ') continue;

            int v = strHex.indexOf(strN.charAt(i));

            if (v < 0) {
                result = -1;
                break;
            }

            result += v * fctr;

            if (fctr == 16) fctr = 1;
            else fctr = 16;

        }

        if (result < 0) {
            strResult = "Non-hex character entered";
        } else if (fctr == 1) {
            strResult = "Odd number of characters entered. e.g. correct value = aa aa";
        } else {
            result = (~(result & 0xff) + 1) & 0xFF;
            strResult = strHex.charAt((int) Math.floor(result / 16)) + String.valueOf(strHex.charAt(result % 16));
        }

        return strResult;

    }

    private static boolean containsMemory(String memory, LinkedList<MemoryValue> memories) {
        for(MemoryValue memoryValue : memories)
            if(memory.equals(memoryValue.getOutput()))
                return true;
        return false;
    }

    private static MemoryValue.MemoryPosition getMemoryPosition(String memory, LinkedList<MemoryValue> memories) {

        for(MemoryValue memoryValue : memories)
            if (memoryValue.getValue().equals(memory))
                return memoryValue.getPosition();

        return MemoryValue.MemoryPosition.NONE;

    }

    public static Stage getStage() {
        return stage;
    }

    public static URL getResource(String uri) {
        return instance.getClass().getResource(uri);
    }

    public static InputStream getResourceAsStream(String uri) {
        return instance.getClass().getResourceAsStream(uri);
    }

}
