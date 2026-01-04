import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

public class Parser {

	private Scanner fileReader;
	private String currentInstruction;
	private VMCommand currentCommand;

	public Parser(String filePath) {

		try {
			fileReader = new Scanner(new FileReader(filePath));
		} catch (FileNotFoundException e) {
			throw new RuntimeException("Could not find the file: " + filePath, e);
		}
	}

	public boolean hasMoreLines() {
		return fileReader.hasNextLine();
	}

	public void advance() {

		if (!hasMoreLines()) {
			System.out.println("Therer are no more rows to read in the file.");
			fileReader.close();
			System.out.println("Scanner was closed.");
		}
		
		// parse only non empty lines and non comment lines
		do {
		    currentInstruction = fileReader.nextLine().trim();

		} while (currentInstruction.isEmpty() || currentInstruction.startsWith("//"));
	
	    // remove in line comments before parsing
	    currentInstruction = currentInstruction.split("//")[0].trim();
	    
//	    System.out.println(currentInstruction);
		currentCommand = parseInstruction(currentInstruction);
	}
	
	public VMCommand currentCommand() {
		return currentCommand;
	}

	public CommandType commandType() {
		return currentCommand.commandType();
	}

//	public String arg1() {
//		if (currentCommand.commandType().equals(CommandType.C_ARITHMETIC)) {
//			return currentCommand.arithmeticOperation().toString();
//		}
//		return currentCommand.memorySegment().toString();
//	}
//
//	public Integer arg2() {
//		if (!currentCommand.commandType().equals(CommandType.C_ARITHMETIC)) {
//			return currentCommand.location();
//		}
//		throw new UnsupportedOperationException("arg2 not available for this command type");
//	}

	private VMCommand parseInstruction(String instruction) {

		String[] parts = instruction.trim().split("\\s+");

		if (parts.length == 0) {
			throw new IllegalArgumentException("Empty instruction");
		}

		switch (parts[0]) {

		case "push": 
			if (parts.length != 3) {
				throw new IllegalArgumentException("Invalid push instruction: " + instruction);
			}
			return new VMCommand(CommandType.C_PUSH, MemorySegment.fromString(parts[1]), Integer.parseInt(parts[2]),
					null);
		

		case "pop":
			if (parts.length != 3) {
				throw new IllegalArgumentException("Invalid pop instruction: " + instruction);
			}
			return new VMCommand(CommandType.C_POP, MemorySegment.fromString(parts[1]), Integer.parseInt(parts[2]),
					null);
		

		default: return new VMCommand(CommandType.C_ARITHMETIC, null, null, ArithmeticOperation.fromString(parts[0]));
		}
	}
}