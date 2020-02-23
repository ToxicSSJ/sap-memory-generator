package me.itoxic.sap.base;

public class MemoryValue {

    private String value;
    private MemoryPosition position = MemoryPosition.NONE;

    public MemoryValue(String value) {

        if(value.length() > 2)
            throw new RuntimeException("El valor es invalido.");

        this.value = value;

    }

    public MemoryValue setPosition(MemoryPosition memoryPosition) {
        this.position = memoryPosition;
        return this;
    }

    public MemoryPosition getPosition() {
        return position;
    }

    public String getValue() {
        return value;
    }

    public String getOutput() {
        return value.length() == 1 ? "0" + value : value;
    }

    public String toString() {
        return "[" + getOutput() + "{" + getPosition().getLiteral() + "}]";
    }

    public enum MemoryPosition {
        NONE(-1, "UNKNOW"),
        _0(0, "0"),
        _1(1, "1"),
        _2(2, "2"),
        _3(3, "3"),
        _4(4, "4"),
        _5(5, "5"),
        _6(6, "6"),
        _7(7, "7"),
        _8(8, "8"),
        _9(9, "9"),
        _A(10, "A"),
        _B(11, "B"),
        _C(12, "C"),
        _D(13, "D"),
        _E(14, "E"),
        _F(15, "F")

        ;

        private int pos;
        private String literal;

        MemoryPosition(int pos, String literal) {
            this.pos = pos;
            this.literal = literal;
        }

        public static MemoryPosition getByPosition(int pos) {
            for(MemoryPosition memoryPosition : MemoryPosition.values()) {
                if(memoryPosition.getPos() == pos) {
                    return memoryPosition;
                }
            }
            return MemoryPosition.NONE;
        }

        public int getPos() {
            return pos;
        }

        public String getLiteral() {
            return literal;
        }

        public MemoryPosition next() {

            if(pos + 1 > 15)
                return MemoryPosition.NONE;

            return getByPosition(pos + 1);

        }

    }

}
