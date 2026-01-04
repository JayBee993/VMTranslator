import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class CodeWriter {

    private final BufferedWriter writer;
    // keeps track of the number of the comparisons that have taken place
    int comparisonCounter = 0;

    public CodeWriter(Path outputFile) throws IOException {
        this.writer = Files.newBufferedWriter(outputFile);
    }
    
    
    public void write(VMCommand currentCommand) throws IOException {

    	switch (currentCommand.commandType()) {

        case C_POP:
            switch (currentCommand.memorySegment()) {

                case STATIC:
                    writePopStatic(currentCommand);
                    break;

                case TEMP:
                    writePopTemp(currentCommand);
                    break;

                case POINTER:
                    writePopPointer(currentCommand);
                    break;

                case LOCAL:
                case ARGUMENT:
                case THIS:
                case THAT:
                    writePushPopStandard(currentCommand);
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected value: " + currentCommand);
            }
            break;

        case C_PUSH:
            switch (currentCommand.memorySegment()) {

                case CONSTANT:
                    writePushConstant(currentCommand);
                    break;

                case STATIC:
                    writePushStatic(currentCommand);
                    break;

                case TEMP:
                    writePushTemp(currentCommand);
                    break;

                case POINTER:
                    writePushPointer(currentCommand);
                    break;

                case LOCAL:
                case ARGUMENT:
                case THIS:
                case THAT:
                    writePushPopStandard(currentCommand);
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected value: " + currentCommand);
            }
            break;

        case C_ARITHMETIC:
            switch (currentCommand.arithmeticOperation()) {

                case ADD:
                case SUB:
                case AND:
                case OR:
                case NOT:
                case NEG:
                    writeArithmeticLogic(currentCommand);
                    break;

                case EQ:
                case LT:
                case GT:
                    writeComparison(currentCommand);
                    break;

                default:
                    throw new IllegalArgumentException("Unexpected value: " + currentCommand);
            }
            break;

        default:
            throw new IllegalArgumentException("Unexpected value: " + currentCommand);
    }

	}
    
    public void close() throws IOException {
        writer.close();
    }

    private void writeLine(String line) throws IOException {
        writer.write(line);
        writer.newLine(); // platform-independent line break
    }
    

    private void writePushConstant(VMCommand currentCommand) throws IOException {
    	String location = String.valueOf(currentCommand.location());
    	
    	// D=i
    	writeLine("//D=" + location);
    	writeLine("@" + location);
    	writeLine("D=A");
    	
    	// RAM[SP]=D
    	writeLine("//RAM[SP]=D");
    	pushDvaluetoStack();
    	
    	// SP++
    	incrementStackPointer();
    }
       
    // pop/push VM command for the Memorysegment {local|argument|this|that} to location i 
    private void writePushPopStandard(VMCommand currentCommand) throws IOException {
    	String location = String.valueOf(currentCommand.location());
    	String memorySegment = currentCommand.memorySegment().symbol();
    	
    	// addr <- segmentPointer+i
    	writeLine("// addr <- "+ memorySegment + "+" + location);
    	writeLine("@" + location);
    	writeLine("D=A");
    	writeLine("@" + memorySegment);
    	writeLine("D=D+M");
    	writeLine("@addr"); // save the address temporarily
    	writeLine("M=D");
    	
    	if(currentCommand.commandType().equals(CommandType.C_POP)) {
        	// SP--
    		decrementStackPointer();
        	
        	// RAM[addr] <- RAM[SP]
        	writeLine("// RAM[addr] <- RAM[SP]");
        	popStackValueandSavetoD();
        	writeLine("@addr");
        	writeLine("A=M");
        	writeLine("M=D");
    	}
 	
    	if(currentCommand.commandType().equals(CommandType.C_PUSH)) {
  	
        	// RAM[SP] <- RAM[addr]
        	writeLine("// RAM[SP] <- RAM[addr]");
        	writeLine("@addr");
        	writeLine("A=M");
        	writeLine("D=M");
        	pushDvaluetoStack();
        	
        	// SP++
    		incrementStackPointer();
    	}	
    } 
    
    // push from Stack to the static Memorysegment
    private void writePushStatic(VMCommand currentCommand) throws IOException {
    	String location = String.valueOf(currentCommand.location());
    	// RAM[SP] <- Foo.i
    	writeLine("// RAM[SP] <- Foo." + location);
    	writeLine("@Foo." + location);
    	writeLine("D=M");
    	pushDvaluetoStack();
    	
    	//SP ++
    	incrementStackPointer();
    }
    
    // pop from Stack to the static Memorysegment
    private void writePopStatic(VMCommand currentCommand) throws IOException {
    	String location = String.valueOf(currentCommand.location());
    	
    	//SP --
    	decrementStackPointer();
    	
    	// Foo.i <- RAM[SP] 
    	writeLine("// Foo." + location + "<- RAM[SP]");
    	popStackValueandSavetoD();    	
    	writeLine("@Foo." + location);
    	writeLine("M=D");    	
    }
    
    // push from Stack to the temp Memorysegment
    private void writePushTemp(VMCommand currentCommand) throws IOException {
    	String location = String.valueOf(currentCommand.location());
    	// RAM[SP] <- RAM[5+i]
    	writeLine("// RAM[SP] <- RAM[5+" + location + "]");
    	writeLine("@" + location);
    	writeLine("D=A");
    	writeLine("@5");
    	writeLine("A=D+A");
    	writeLine("D=M");
    	pushDvaluetoStack();
    	
    	//SP ++
    	incrementStackPointer();
    }
    
    // pop from Stack to the temp Memorysegment
    private void writePopTemp(VMCommand currentCommand) throws IOException {
    	String location = String.valueOf(currentCommand.location());
    	
    	//SP --
    	decrementStackPointer();
    	
    	// RAM[5+i] <- RAM[SP] 
    	writeLine("// RAM[5+" + location + "] <- RAM[SP]");
    	writeLine("@" + location);
    	writeLine("D=A");
    	writeLine("@5");
    	writeLine("D=D+A");
    	writeLine("@addr");
    	writeLine("M=D");
    	
    	//SP --
//    	decrementStackPointer();
    	popStackValueandSavetoD(); 
    	
    	writeLine("@addr");
    	writeLine("A=M");
    	writeLine("M=D");    	
    }
    
    // push from Stack to THIS(0)/THAT(1) pointer
    private void writePushPointer(VMCommand currentCommand) throws IOException {
    	Integer whichPointer = currentCommand.location();
    	String pointer = "THIS";
    	if(whichPointer.equals(1)) {
    		pointer = "THAT";
    	}

    	writeLine("@" + pointer);
    	writeLine("D=M");

    	pushDvaluetoStack();
    	
    	//SP ++
    	incrementStackPointer();
    }
    
    // pop from Stack to THIS(0)/THAT(1) pointer
    private void writePopPointer(VMCommand currentCommand) throws IOException {
    	Integer whichPointer = currentCommand.location();
    	String pointer = "THIS";
    	if(whichPointer.equals(1)) {
    		pointer = "THAT";
    	}
    	
    	//SP --
    	decrementStackPointer();   	
    	popStackValueandSavetoD();

    	writeLine("@" + pointer);
    	writeLine("M=D");
    }
    
    private void writeArithmeticLogic(VMCommand cmd) throws IOException {
        String asm;
        switch (cmd.arithmeticOperation()) {
            case ADD: asm = "D=D+M"; break;
            case SUB: asm = "D=M-D"; break;
            case AND: asm = "D=D&M"; break;
            case OR:  asm = "D=D|M"; break;
            case NEG: asm = "M=-M"; break;
            case NOT: asm = "M=!M"; break;
            default: throw new IllegalArgumentException("Unexpected op: " + cmd.arithmeticOperation());
        }

        if(cmd.arithmeticOperation() == ArithmeticOperation.NEG || cmd.arithmeticOperation() == ArithmeticOperation.NOT){
            writeLine("// SP--");
            writeLine("@SP");
            writeLine("M=M-1");
            writeLine("A=M");
            writeLine(asm);
            incrementStackPointer();
        } else {
            writeLine("// SP--");
            writeLine("@SP");
            writeLine("M=M-1");
            writeLine("A=M");
            writeLine("D=M");
            writeLine("@SP");
            writeLine("M=M-1");
            writeLine("A=M");
            writeLine(asm);
            writeLine("M=D");
            incrementStackPointer();
        }
    }

    
//    private void writeArithmeticLogic(VMCommand currentCommand) throws IOException {
//    	ArithmeticOperation currentOperation = currentCommand.arithmeticOperation();
//    	
//    	writeLine("// " + currentOperation.toString());
//    	
//    	writeLine("// SP--");
//    	writeLine("@SP");
//    	writeLine("M=M-1");
//    	writeLine("A=M");
//    	
//    	switch (currentOperation) {
//			case NEG:{
//				writeLine("M=-M");
//				incrementStackPointer();
//				break;}
//					
//			case NOT:{
//				writeLine("M=!M");
//				incrementStackPointer();
//				break;}
//			
//			case ADD:
//			case SUB:
//			case AND:
//			case OR: {
//				writeLine("D=M // D <- RAM[SP]=y");
//				
//		    	writeLine("@SP");
//		    	writeLine("M=M-1");
//		    	writeLine("A=M");
//		    	
//		    	if(currentOperation.equals(ArithmeticOperation.ADD)) {
//		    		writeLine("D=D+M // D <- RAM[SP]=x + D=y");
//		    	} else if (currentOperation.equals(ArithmeticOperation.SUB)) {
//		    		writeLine("D=M-D // D <- RAM[SP]=x - D=y");
//		    	} else if (currentOperation.equals(ArithmeticOperation.AND)) {
//		    		writeLine("D=D&M // D <- RAM[SP]=x & D=y");
//		    	} else if (currentOperation.equals(ArithmeticOperation.OR)) {
//		    		writeLine("D=D|M // D <- RAM[SP]=x | D=y");
//		    	}
//  	
//		    	writeLine("M=D");
//		    	incrementStackPointer();
//		    	break;}
//			
//			default: 
//				throw new IllegalArgumentException("Unexpected value: " + currentOperation);
//			}    	    	    	
//    }
    
    private void writeComparison(VMCommand currentCommand) throws IOException {
    	ArithmeticOperation currentOperation = currentCommand.arithmeticOperation();
    	comparisonCounter += 1;
    	
    	writeLine("// " + currentOperation.toString());
    	
    	writeLine("// SP--");
    	writeLine("@SP");
    	writeLine("M=M-1");
    	writeLine("A=M");
		writeLine("D=M // D <- RAM[SP]=y");
		
    	writeLine("@SP");
    	writeLine("M=M-1");
    	writeLine("A=M");
    	writeLine("D=M-D // D <- RAM[SP]=x - D=y");
    	
    	writeLine("@IS_FULLFILLED." + String.valueOf(comparisonCounter));

    	switch (currentOperation) {

	        case EQ:
	            writeLine("D;JEQ");
	            break;
	
	        case LT:
	            writeLine("D;JLT");
	            break;
	
	        case GT:
	            writeLine("D;JGT");
	            break;
	
	        default:
	            throw new IllegalArgumentException("Unexpected value: " + currentOperation);
    	}

		
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=0 // false");
		
		writeLine("@CONTINUE." + String.valueOf(comparisonCounter));
		writeLine("0;JMP");
		
		writeLine("(IS_FULLFILLED." + String.valueOf(comparisonCounter)+ ")");
		writeLine("@SP");
		writeLine("A=M");
		writeLine("M=-1 // true");
		
		writeLine("(CONTINUE." + String.valueOf(comparisonCounter)+ ")");
		incrementStackPointer();
    	
    }
    
    // recurring assembly instructions
    private void incrementStackPointer() throws IOException {
    	writeLine("// SP++");
    	writeLine("@SP");
    	writeLine("M=M+1");
    }
    private void decrementStackPointer() throws IOException {
    	writeLine("// SP--");
    	writeLine("@SP");
    	writeLine("M=M-1");
    }
    private void pushDvaluetoStack() throws IOException {
    	writeLine("@SP");
    	writeLine("A=M");
    	writeLine("M=D");
    }
    private void popStackValueandSavetoD() throws IOException {
    	writeLine("@SP");
    	writeLine("A=M");
    	writeLine("D=M");
    }
}
