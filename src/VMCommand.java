
//public record VMCommand(CommandType commandType, MemorySegment memorySegment, Integer location, ArithmeticOperation arithmeticOperation) {
//
//}

public class VMCommand {
    private final CommandType commandType;
    private final MemorySegment memorySegment;
    private final Integer location;
    private final ArithmeticOperation arithmeticOperation;

    public VMCommand(CommandType commandType,
                     MemorySegment memorySegment,
                     Integer location,
                     ArithmeticOperation arithmeticOperation) {
        this.commandType = commandType;
        this.memorySegment = memorySegment;
        this.location = location;
        this.arithmeticOperation = arithmeticOperation;
    }

    public CommandType commandType() { return commandType; }
    public MemorySegment memorySegment() { return memorySegment; }
    public Integer location() { return location; }
    public ArithmeticOperation arithmeticOperation() { return arithmeticOperation; }
}

