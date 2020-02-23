package me.itoxic.sap;

import java.util.HashMap;
import java.util.Map;

public enum Static {

    LDA("2", "LDA", "LDA"),
    ADD("3", "+", "ADD"),
    SUB("8", "-", "SUB"),
    MUL("A", "*", "MUL"),
    MOD("C", "%", "MOD"),
    CALL("5", "CALL", "CALL"),
    GOTO("7", "GOTO", "GOTO"),
    RETUR("F", "RETURN", "RETUR"),
    OUT("B", "OUT", "OUT"),
    HLT("0", "HALT", "HLT");

    private static Map<Static, String> customLiterals = new HashMap<>();
    private static boolean custom = false;

    private String literal;
    private String asm;
    private String ui = "0";

    Static(String literal, String asm, String ui) {
        this.literal = literal;
        this.asm = asm;
        this.ui = ui;
    }

    public static Static getByLiteral(String literal) {
        for(Static s : Static.values())
            if(s.getLiteral().equals(literal))
                return s;
        return null;
    }

    public static Static getByUI(String ui) {
        for(Static s : Static.values())
            if(s.getUi().equals(ui))
                return s;
        return null;
    }

    public String getLiteral() {

        if(custom && customLiterals.containsKey(this))
            return customLiterals.get(this);
        else if(custom && !customLiterals.containsKey(this))
            return "0";

        return literal;

    }

    public String getAsm() {
        return asm;
    }

    public String getUi() {
        return ui;
    }

    public static void setCustomLiteral(Static custom, String literal) {

        if(literal == null || literal.isEmpty())
            return;

        customLiterals.put(custom, literal);

    }

    public static void turnCustom(boolean customStatic) {
        custom = customStatic;
    }

}
