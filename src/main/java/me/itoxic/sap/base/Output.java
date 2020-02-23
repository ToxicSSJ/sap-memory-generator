package me.itoxic.sap.base;

import java.util.LinkedList;

public class Output {

    private LinkedList<Instruction> instructions;
    private LinkedList<MemoryValue> memoryValues;

    private LinkedList<String> output;
    private String file;

    public Output(LinkedList<Instruction> instructions, LinkedList<MemoryValue> memoryValues) {

        this.instructions = instructions;
        this.memoryValues = memoryValues;

        this.output = new LinkedList<>();
        this.file = "";

    }

    public LinkedList<String> getOutput() {
        return output;
    }

    public Output setOutput(LinkedList<String> output) {
        this.output = output;
        return this;
    }

    public String getFile() {
        return file;
    }

    public Output setFile(String file) {
        this.file = file;
        return this;
    }

    public LinkedList<Instruction> getInstructions() {
        return instructions;
    }

    public Output setInstructions(LinkedList<Instruction> instructions) {
        this.instructions = instructions;
        return this;
    }

    public LinkedList<MemoryValue> getMemoryValues() {
        return memoryValues;
    }

    public Output setMemoryValues(LinkedList<MemoryValue> memoryValues) {
        this.memoryValues = memoryValues;
        return this;
    }

}
