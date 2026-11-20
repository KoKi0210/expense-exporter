package com.example.expenseexporter.formula;

public class FormulaSyntaxException extends RuntimeException {

    private final int position;

    public FormulaSyntaxException(String message) {
        super(message);
        this.position = 0;
    }

    FormulaSyntaxException(String problem, String source, int index) {
        super(problem + " at position " + (index + 1) + " in '" + source + "'");
        this.position = index + 1;
    }

    public int position() {
        return position;
    }
}
