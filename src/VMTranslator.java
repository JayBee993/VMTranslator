import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class VMTranslator {
	
    public static void main(String[] args) throws IOException {
    	
    	// Project 7
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
        
    	// Project 8
        if (args.length != 1) {
            System.err.println("Usage: java VMTranslator <input.vm> or <inputfolder>");
            System.exit(1);
        }

        Path inputPath = Path.of(args[0]);
        
        // one VM file
        if (inputPath.toString().endsWith(".vm")) {
        	 Path outputPath = Path.of(
        	            inputPath.toString().replace(".vm", ".asm")
        	        );

        	        Parser parser = new Parser(inputPath.toString());
        	        CodeWriter writer = new CodeWriter(outputPath);
        	        
        	        while (parser.hasMoreLines()) {
        	            parser.advance();
        	            writer.write(parser.currentCommand());
        	        }

        	        writer.close();
        } else {
        
        	// input a folder name
        	String endDestination = inputPath.getFileName().toString();
        	
//        	System.out.println(endDestination);
        	
    		// collecting all the VM files in the folder 
			try (Stream<Path> paths = Files.list(inputPath)) {
				List<Path> pathList = paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".vm"))
						.collect(Collectors.toList());

				Path outputPath = Path.of(args[0] + "\\" + endDestination + ".asm");
				CodeWriter writer = new CodeWriter(outputPath);
				
				writer.writeInitialize();
				
				for (Path file : pathList) {

//					System.out.println(file.toString());

					Parser parser = new Parser(file.toString());

					while (parser.hasMoreLines()) {
						parser.advance();
						writer.write(parser.currentCommand());
					}

				}
				writer.close();
			}
		}
        
       
    }
    
    
// Test locally:    
//	public static void main(String[] args) throws IOException {		
//	//	List<String> files = List.of("BasicTest","PointerTest","SimpleAdd","StackTest","StaticTest");
//		
//////		List<String> files = List.of("BasicLoop","FibonacciSeries");
//////		List<String> files = List.of("FibonacciElement");
//////		List<String> files = List.of("NestedCall");
////		List<String> files = List.of("StaticsTest");
////		
////		for(String file : files) {
////			
//////			String filePath = "C:\\Nand2Tetris\\Project07\\VMFilestoTranslate\\"+ file +".vm";
//////			String outputPath = "C:\\Nand2Tetris\\Project07\\VMFilestoTranslate\\"+ file +".asm";
////			String filePath = "C:\\Nand2Tetris\\Project08\\VMfilesToTranslate\\"+ file +".vm";
////			String outputPath = "C:\\Nand2Tetris\\Project08\\VMfilesToTranslate\\"+ file +".asm";
////			
//////			System.out.println(filePath);
////			
////			Parser testParser = new Parser(filePath);
////			CodeWriter testWriter = new CodeWriter(Path.of(outputPath));
////			
////			
////			while( testParser.hasMoreLines()) {
////				testParser.advance();
////				testWriter.write(testParser.currentCommand());
////			}
////			testWriter.close();
////		}
//
//	// collecting all the VM files in a folder
//	Path dir = Paths.get("C:\\Nand2Tetris\\Project08\\VMfilesToTranslate\\FibonacciElement");
//
//	try (Stream<Path> paths = Files.list(dir)) {
//		List<Path> result = paths.filter(Files::isRegularFile).filter(p -> p.toString().endsWith(".vm"))
//				.collect(Collectors.toList());
//
//		Path outputPath = Path
//				.of("C:\\Nand2Tetris\\Project08\\VMfilesToTranslate\\FibonacciElement\\FibonacciElement.asm");
//		CodeWriter testWriter = new CodeWriter(outputPath);
//		testWriter.writeInitialize();
//		for (Path file : result) {
//
//			System.out.println(file.toString());
//
//			Parser testParser = new Parser(file.toString());
//
//			while (testParser.hasMoreLines()) {
//				testParser.advance();
//				testWriter.write(testParser.currentCommand());
//			}
//
//		}
//		testWriter.close();
//	}
//}
}

