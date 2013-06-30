package ru.isk.cecriteria;

/**
 * @author imustafin
 */
public enum SimpleOperand {
    EQ ("="),
    NOT_EQ ("<>"),
    LT ("<"),
    LE ("<="),
    GT (">"),
    GE (">=");

    private String operandAsString;

    SimpleOperand(String operandAsString) {
        this.operandAsString = operandAsString;
    }

    public String stringValue() {
        return operandAsString;
    }
}
