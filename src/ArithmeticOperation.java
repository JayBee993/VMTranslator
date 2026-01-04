
public enum ArithmeticOperation {
	ADD,
	SUB,
	NEG,
	EQ,
	GT,
	LT,
	AND,
	OR,
	NOT;
	
    public static ArithmeticOperation fromString(String s) {
        return ArithmeticOperation.valueOf(s.toUpperCase());
    }
}
