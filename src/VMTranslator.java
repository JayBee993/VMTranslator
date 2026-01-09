import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

public class VMTranslator {
	
//    public static void main(String[] args) throws IOException {
//
//        if (args.length != 1) {
//            System.err.println("Usage: java VMTranslator <input.vm>");
//            System.exit(1);
//        }
//
//        Path inputPath = Path.of(args[0]);
//
//        if (!inputPath.toString().endsWith(".vm")) {
//            System.err.println("Input file must end with .vm");
//            System.exit(1);
//        }
//
//        Path outputPath = Path.of(
//            inputPath.toString().replace(".vm", ".asm")
//        );
//
//        Parser parser = new Parser(inputPath.toString());
//        CodeWriter writer = new CodeWriter(outputPath);
//
//        while (parser.hasMoreLines()) {
//            parser.advance();
//            writer.write(parser.currentCommand());
//        }
//
//        writer.close();
//    }
    
    
// Test locally:    
	public static void main(String[] args) throws IOException {		
	//	List<String> files = List.of("BasicTest","PointerTest","SimpleAdd","StackTest","StaticTest");
		
		List<String> files = List.of("BasicLoop","FibonacciSeries");
		
		for(String file : files) {
			
//			String filePath = "C:\\Nand2Tetris\\Project07\\VMFilestoTranslate\\"+ file +".vm";
//			String outputPath = "C:\\Nand2Tetris\\Project07\\VMFilestoTranslate\\"+ file +".asm";
			String filePath = "C:\\Nand2Tetris\\Project08\\VMfilesToTranslate\\"+ file +".vm";
			String outputPath = "C:\\Nand2Tetris\\Project08\\VMfilesToTranslate\\"+ file +".asm";
			
			System.out.println(filePath);
			
			Parser testParser = new Parser(filePath);
			CodeWriter testWriter = new CodeWriter(Path.of(outputPath));
			
			
			while( testParser.hasMoreLines()) {
				testParser.advance();
				testWriter.write(testParser.currentCommand());
			}
			testWriter.close();
		}
	
	}
    
}

