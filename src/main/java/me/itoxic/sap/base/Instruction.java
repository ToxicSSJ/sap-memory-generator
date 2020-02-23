package me.itoxic.sap.base;

import me.itoxic.sap.Static;

public class Instruction {

    String instructionValue;
    String memoryValue = "0";

    public Instruction(String instructionValue, String memoryValue) {
        this.instructionValue = instructionValue;
        this.memoryValue = memoryValue;
    }

    public String getLiteralInstruction() {
        return Static.getByLiteral(instructionValue).toString();
    }

    public String getInstructionValue() {
        return instructionValue;
    }

    public String getMemoryValue() {
        return memoryValue;
    }

    public String getOutput() {
        return instructionValue + memoryValue;
    }

    public String toString() {
        return "[" + getOutput() + "]";
    }

}
