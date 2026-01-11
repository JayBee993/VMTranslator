import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class CodeWriter {

    private final BufferedWriter writer;
    // keeps track of the number of the comparisons that have taken place
    int comparisonCounter = 0;
    
    boolean previousCommandledtoBoolean = false;
    
    // the following two fields are important for the returnAddrLabels
    String currentfunctionName;
    int returnLabelnumber;

    public CodeWriter(Path outputFile) throws IOException {
        this.writer = Files.newBufferedWriter(outputFile);
    }
    
    public void writeInitialize() throws IOException {
    	// SP = 261
    	writeLine("// SP = 261");
    	writeLine("@261");
    	writeLine("D=A");
    	writeLine("@SP");
    	writeLine("M=D");
    	

    	// goto Sys.init
    	writeLine("@Sys.init");
    	writeLine("0;JMP");  	
    }
    
    public void write(VMCommand currentCommand) throws IOException {
    	
//    	System.out.println(currentCommand.commandType().toString());

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
            
        case C_LABEL:
        	writeLabel(currentCommand);
        	break;
        case C_GOTO:
        	writeGoto(currentCommand);
        	break;
        case C_IF:
        	writeIfGoto(currentCommand);
        	break;
        case C_FUNCTION:
        	writeFunction(currentCommand);
        	break;
        case C_CALL:
        	writeCall(currentCommand);
        	break;
        case C_RETURN:
        	writeReturn(currentCommand);
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
    
    private void writeCall(VMCommand currentCommand) throws IOException {
    	// put the current returnAddrLabel together    	
    	String  retAddrLabel = currentfunctionName +"$ret." + String.valueOf(returnLabelnumber);
    	
    	// push retAddrLabel (return address label)
    	writeLine("// call " + currentCommand.functionName());
    	writeLine("// push retAddrLabel ");
    	
    	writeLine("@"+retAddrLabel);
    	writeLine("D=A");
    	writeLine("@SP");
    	writeLine("A=M");
    	writeLine("M=D");
    	//SP ++
    	writeLine("@SP");
    	writeLine("M=M+1");
    	
    	// push LCL, ARG, THIS, THAT
//    	List<String> memorySegments = List.of("LCL","ARG","THIS","THAT");
    	List<String> memorySegments = Arrays.asList("LCL","ARG","THIS","THAT");
    	
    	for(String segment : memorySegments ) {
    		writeLine("// push " + segment);
        	writeLine("@" + segment);
        	writeLine("D=M");
        	writeLine("@SP");
        	writeLine("A=M");
        	writeLine("M=D");
        	//SP ++
        	writeLine("@SP");
        	writeLine("M=M+1");
    	}
    	
    	// ARG=SP-5-nArgs
    	writeLine("// ARG=SP-5-nArgs");
    	writeLine("@SP");
    	writeLine("D=M");
    	writeLine("@5");
    	writeLine("D=D-A");
    	writeLine("@" + String.valueOf(currentCommand.numberOfArguments()));
    	writeLine("D=D-A");
    	writeLine("@ARG");
    	writeLine("M=D");
    	
    	// LCL=SP
    	writeLine("// LCL=SP");
    	writeLine("@SP");
    	writeLine("D=M");
    	writeLine("@LCL");
    	writeLine("M=D");
    	
    	// goto functionName of the callee (called function)
    	writeLine("// goto " + currentCommand.functionName());
    	writeLine("@" + currentCommand.functionName());
    	writeLine("0;JMP");
    	
    	// (retAddrLabel) injects a label into the code
    	writeLine("("+retAddrLabel+")");
    	
    	// increase the returnLabelnumber by 1
    	returnLabelnumber += 1;
    }
    
    private void writeFunction(VMCommand currentCommand) throws IOException {
    	
    	// set the current functionname of the caller
    	currentfunctionName = currentCommand.functionName();
    	// initialize the returnLabelbumber to 1
    	returnLabelnumber = 1;
    	
    	writeLine("(" + currentfunctionName + ")");
    	
    	// push 0 nVars times
    	writeLine("// push 0 nVars= "+ String.valueOf(currentCommand.numberOfLocalvariables()) +" times");
    	writeLine("@0");
    	writeLine("D=A");
    	for(int i=0; i < currentCommand.numberOfLocalvariables(); i++) {
        	// RAM[SP]=0
        	writeLine("//RAM[SP]=0");
        	pushDvaluetoStack();
        	
        	// SP++
        	incrementStackPointer();		
    	}
    }
    
    private void writeReturn(VMCommand currentCommand) throws IOException {   	
    	// return
    	writeLine("// return");
    	
    	// endFrame = LCL
    	writeLine("// endFrame = LCL");
    	writeLine("@LCL");
    	writeLine("D=M");
    	writeLine("@R13"); // R13 = endFrame
    	writeLine("M=D");
    	
    	// retAddr = *(endFrame-5)
    	writeLine("// retAddr = *(endFrame-5)"); // in D there is still the address of LCL= endframe saved
    	writeLine("@5");
    	writeLine("A=D-A"); // A = endframe -5
    	writeLine("D=M");   // D = *(endframe-5)
    	writeLine("@R14"); // R14 = retAddr
    	writeLine("M=D");
    	
    	// *ARG = pop()
    	writeLine("// *ARG = pop()");
    	writeLine("@SP"); //SP--
    	writeLine("M=M-1");
    	writeLine("A=M");
    	writeLine("D=M");
    	writeLine("@ARG");
    	writeLine("A=M");
    	writeLine("M=D");
    	
    	// SP=ARG+1
    	writeLine("// SP=ARG+1");
    	writeLine("@ARG");
    	writeLine("D=M");
    	writeLine("@SP");
    	writeLine("M=D+1");
    	
    	// THAT=*(endFrame-1)
    	writeLine("// THAT=*(endFrame-1)");
    	writeLine("@R13"); // = endFrame
    	writeLine("D=M"); // D = endframe
    	writeLine("@1");
    	writeLine("A=D-A"); // A = endframe -1
    	writeLine("D=M");   // D = *(endframe-1), Wert an der Adresse
    	writeLine("@THAT");
    	writeLine("M=D");
    	    	
    	// THIS=*(endFrame-2)
    	writeLine("// THIS=*(endFrame-2)");
    	writeLine("@R13"); // = endFrame
    	writeLine("D=M"); // D = endframe
    	writeLine("@2");
    	writeLine("A=D-A"); // A = endframe -2
    	writeLine("D=M");   // D = *(endframe-2), Wert an der Adresse
    	writeLine("@THIS");
    	writeLine("M=D");
    	
    	// ARG =*(endFrame-3)
    	writeLine("// ARG =*(endFrame-3)");
    	writeLine("@R13"); // = endFrame
    	writeLine("D=M"); // D = endframe
    	writeLine("@3");
    	writeLine("A=D-A"); // A = endframe -3
    	writeLine("D=M");   // D = *(endframe-3), Wert an der Adresse
    	writeLine("@ARG");
    	writeLine("M=D");
    	
    	// LCL =*(endFrame-4)
    	writeLine("// LCL =*(endFrame-4)");
    	writeLine("@R13"); // = endFrame
    	writeLine("D=M"); // D = endframe
    	writeLine("@4");
    	writeLine("A=D-A"); // A = endframe -4
    	writeLine("D=M");   // D = *(endframe-4), Wert an der Adresse
    	writeLine("@LCL");
    	writeLine("M=D");
    	
		// goto retAddr
    	writeLine("@R14"); // retAddr
    	writeLine("A=M");  // make sure to jump to the address and not to RAM[4]
    	writeLine("0;JMP");	
    }
    
    private void writeLabel(VMCommand currentCommand) throws IOException {
    	writeLine("(" + currentfunctionName+"$"+currentCommand.label() + ")");
	}
    
    private void writeGoto(VMCommand currentCommand) throws IOException {
    	writeLine("@" + currentfunctionName+"$"+ currentCommand.label());
    	writeLine("0;JMP");
	}
    
    private void writeIfGoto(VMCommand currentCommand) throws IOException {
    	if(previousCommandledtoBoolean) {
        	// case of a boolean value
        	writeLine("@SP");
        	writeLine("M=M-1");
        	writeLine("A=M");
        	writeLine("D=M+1"); // true = -1, if a evaluation is positive the value -1 is written on top of the stack
        	
        	writeLine("@" + currentfunctionName +"$"+ currentCommand.label());
        	writeLine("D;JEQ");
    	} else {
        	// case if we don't have an boolean value on the stack, in this case it is checked if the stackvalue is >0
        	writeLine("@SP");
        	writeLine("M=M-1");
        	writeLine("A=M");
        	writeLine("D=M");
        	
        	writeLine("@" + currentfunctionName+"$"+ currentCommand.label());
        	writeLine("D;JGT");
    	}
    	
    	// set the booloean back
    	previousCommandledtoBoolean = false;
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
    	writeLine("@R15"); //@addr save the address temporarily
    	writeLine("M=D");
    	
    	if(currentCommand.commandType().equals(CommandType.C_POP)) {
        	// SP--
    		decrementStackPointer();
        	
        	// RAM[addr] <- RAM[SP]
        	writeLine("// RAM[addr] <- RAM[SP]");
        	popStackValueandSavetoD();
        	writeLine("@R15"); //@addr
        	writeLine("A=M");
        	writeLine("M=D");
    	}
 	
    	if(currentCommand.commandType().equals(CommandType.C_PUSH)) {
  	
        	// RAM[SP] <- RAM[addr]
        	writeLine("// RAM[SP] <- RAM[addr]");
        	writeLine("@R15"); //@addr
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
    	writeLine("@"+currentfunctionName.split("\\.")[0]+"$Foo." + location);
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
    	writeLine("@"+currentfunctionName.split("\\.")[0] + "$Foo." + location);
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
    	writeLine("@R15"); //@addr
    	writeLine("M=D");
    	
    	//SP --
//    	decrementStackPointer();
    	popStackValueandSavetoD(); 
    	
    	writeLine("@R15"); //@addr
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
		
		previousCommandledtoBoolean = true;
    	
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
