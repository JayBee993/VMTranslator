
public enum MemorySegment {
    CONSTANT(""),
    LOCAL("LCL"),
    ARGUMENT("ARG"),
    THIS("THIS"),
    THAT("THAT"),
    STATIC(""),
    TEMP(""),
    POINTER("");
	
    private final String symbol;

    MemorySegment(String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }

    public static MemorySegment fromString(String s) {
        return MemorySegment.valueOf(s.toUpperCase());
    }   
}

