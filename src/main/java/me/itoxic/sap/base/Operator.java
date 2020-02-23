package me.itoxic.sap.base;

import me.itoxic.sap.Static;

public enum Operator {

    ADD('+', Static.ADD),
    MINUS('-', Static.SUB),
    MULTIPLY('*', Static.MUL),
    MODULE('%', Static.MOD),
    GOTO('g', Static.GOTO),
    RETURN('r', Static.RETUR),
    CALL('c', Static.CALL)

    ;

    private char current;
    private Static ins;

    Operator(char current, Static ins) {

        this.current = current;
        this.ins = ins;

    }

    public char getCharacter() {
        return current;
    }

    public Static getInstruction() {
        return ins;
    }

    public static Operator check(char current) {

        for(Operator operator : Operator.values())
            if(operator.current == current)
                return operator;

        return null;

    }

}
