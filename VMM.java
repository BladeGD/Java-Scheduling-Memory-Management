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
		command[] commands;
		
		for(String input: commandFile){
			
		}

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