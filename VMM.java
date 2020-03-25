import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;

public class VMM extends Thread {

    public static void main (String args[]) throws FileNotFoundException{

        String[] memconfig = readFile("memconfig.txt").split("\\s+");
        int memSize = Integer.parseInt(memconfig[0]);

        String[] commandFile = readFile("command.txt").split("\n)");
        
        VMM v = new VMM();
		
		
		for(String input: commandFile){
			String[] commands = input.split(" ");
			
			//
			switch (commands[0].toLowerCase()) {
				case "store": v.memStore(Integer.parseInt(commands[1]),Integer.parseInt(commands[2]));
				case "release": v.memFree(Integer.parseInt(commands[1]));
				case "lookup": v.memLookup(Integer.parseInt(commands[1]));
				default: System.out.println("Command "+commands[0]+" was not recognized");
			}
		}

    }
    
    //API
    public void memStore(int variableId, int value) {
    	
    }
    
    public void memFree(int variableId) {
    	
    }
    
    public void memLookup(int variableId) {
    	
    }

    private static String readFile(String name) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(name)));
			StringBuilder sb = new StringBuilder();
			String line = "";
			
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			
			return sb.toString();
		} catch (Exception e) {
			return null;
		}
    }
}