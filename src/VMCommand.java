
//public record VMCommand(CommandType commandType, MemorySegment memorySegment, Integer location, ArithmeticOperation arithmeticOperation) {
//
//}

public class VMCommand {
    private final CommandType commandType;
    private final MemorySegment memorySegment;
    private final Integer location;
    private final ArithmeticOperation arithmeticOperation;
    private final String label;
    private final String functionName;
    private final Integer nArgs;
    private final Integer nVars;

    public VMCommand(CommandType commandType,
                     MemorySegment memorySegment,
                     Integer location,
                     ArithmeticOperation arithmeticOperation,
                     String label,
                     String functionName,
                     Integer nArgs,
                     Integer nVars) {
        this.commandType = commandType;
        this.memorySegment = memorySegment;
        this.location = location;
        this.arithmeticOperation = arithmeticOperation;
        this.label = label;
        this.functionName = functionName;
        this.nArgs = nArgs;        
        this.nVars = nVars;        
    }

    public CommandType commandType() { return commandType; }
    public MemorySegment memorySegment() { return memorySegment; }
    public Integer location() { return location; }
    public ArithmeticOperation arithmeticOperation() { return arithmeticOperation; }
    public String label() { return label; }
    public String functionName() { return functionName; }
    public Integer numberOfArguments() { return nArgs; }
    public Integer numberOfLocalvariables() { return nVars; }
}

