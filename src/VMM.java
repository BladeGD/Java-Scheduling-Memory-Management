//Driver is in Scheduler.java

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.Random;

public class VMM extends Thread {

	private static int memSize;
	private static mem[] mainMemory;
	private static int counter = 0;
	protected static int commandCounter = 0;
	protected static Proc procRunning;
	protected static Semaphore procSync = new Semaphore(0);
	private String[] commandFile;
	private int nextCommand;
	private Random rand = new Random();
	private static int tempTime = 0;
	private static int swapTime = 0;
	protected static Boolean isSwap = false;

	
	public VMM() {
		String[] memconfig = readFile("memconfig.txt").split("\\s+");
		memSize = Integer.parseInt(memconfig[0]);
		mainMemory = new mem[memSize];
		nextCommand = 0;
		commandFile = readFile("command.txt").split("\n");
		start();
		try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		//Busy spin until scheduler is done. Only using VMM to access its API
		while(scheduler.finishedCounter != scheduler.size); 
	}
	
	public void API(){
		
		int result;
		if(nextCommand < commandFile.length) {
			String[] commands = commandFile[nextCommand].split(" ");
			tempTime = generateExecTime();
			
							
			switch (commands[0].toLowerCase()) {
			case "store": 
				scheduler.pw.println("Clock: " + (scheduler.time * 1000 - tempTime) + ", Process " + procRunning.getProcessName() + ", Store: Variable " + commands[1] + ", Value: " + commands[2]);
				result = memStore(Integer.parseInt(commands[1]),Integer.parseInt(commands[2]));
				break;
			case "release": 
				scheduler.pw.println("Clock: " + (scheduler.time * 1000 - tempTime) + ", Process " + procRunning.getProcessName() + ", Release: Variable " + commands [1]);
				result = memFree(Integer.parseInt(commands[1]));
				if(result == -1) {
					scheduler.pw.println("Release Attempt Failed, " + commands[1] + " not found.");
				}
				break;
			case "lookup": 
				scheduler.pw.println("Clock: " + (scheduler.time * 1000 - tempTime) + ", Process " + procRunning.getProcessName() + ", Lookup: Variable " + commands [1] + ", Value: " + memLookup(Integer.parseInt(commands[1])));
				break;
			default: scheduler.pw.println("Command "+commands[0]+" was not recognized");
			}
			
		}
		nextCommand++;
		procSync.release();
		
	}

    //API
    public int memStore(int variableId, int value) {

		//if already exist in MM
			//overwrite in MM
		//else if already exist in VM
			//if write to MM if free slot
			//else (no avaliable spot) swap oldest from MM to VM and write to MM
		//else (it doesn't exist)
			//swap and insert
			//swapped value is stored in VM
		
		for(int i = 0; i < memSize; i++) {// exist in MM
			if(mainMemory[i] != null) {
				if(mainMemory[i].getID() == variableId) {
					mainMemory[i].setVal(value);
					return mainMemory[i].getVal();
				}
			}
		}

		ArrayList<mem> tempMem1 = fromVM();
		ArrayList<mem> tempMem = tempMem1;
		int index;
		for (mem m: tempMem1) {
			if(m.getID() == variableId) {
				if(counter < memSize) { //Free spot avaliable
					index = emptyFrame();
					mainMemory[index] = m; //insert lookup in MM
					tempMem.remove(tempMem1.indexOf(m)); //remove lookup from disk
					mainMemory[index].setVal(value); // set new value
					counter ++;
					toVM(tempMem);
					return mainMemory[index].getVal();
				}
				else{ //must swap
					index = oldestPage();
					mem swapMem = new mem(mainMemory[index]); //stores page to be swapped
					mainMemory[index] = m; //overwrites page in MM
					mainMemory[index].setVal(value); // set new value
					tempMem.add(swapMem); //add swapped page to VM arraylist
					tempMem.remove(tempMem1.indexOf(m)); // remove lookup from disk
					toVM(tempMem); //writes back to disk
					isSwap = true;
					swapTime = ( rand.nextInt(500));
					scheduler.pw.println("Clock: " +(scheduler.time * 1000 - swapTime) + " Memory Manager, Swap: Variable " + swapMem.getID() +" with Variable " + m.getID());
					return mainMemory[index].getVal();
				}
			}
		}

		mem newMem = new mem(variableId, value, scheduler.time); // doesn't exist 

		if(counter < memSize){
			index = emptyFrame();
			mainMemory[index] = newMem;
			counter++;
			return mainMemory[index].getVal();
		}
		else{
			index = oldestPage();
			mem swapMem = new mem(mainMemory[index]); //stores page to be swapped
			mainMemory[index] = newMem; //overwrites page in MM
			tempMem.add(swapMem); //add swapped page to VM arraylist
			toVM(tempMem); //writes back to disk
			isSwap = true;
			swapTime = (rand.nextInt(500));
			scheduler.pw.println("Clock: " + (scheduler.time * 1000 - swapTime) + " Memory Manager, Swap: Variable " + swapMem.getID() +" with Variable " + newMem.getID());
			return mainMemory[index].getVal();
		}
    }
    
	public int memFree(int variableId) { //delete
		
		//if in MM
		//if in VM
			//swap to MM then release

		for(int i = 0; i < memSize; i++){
			if(mainMemory[i].getID() == variableId){
				mainMemory[i] = null;
				counter--;
				return 1; //returns 1 if successful
			}
		}
		
		ArrayList<mem> tempMem1 = fromVM();
		ArrayList<mem> tempMem = tempMem1;
	
		for (mem m: tempMem1) {
			if(m.getID() == variableId) {
				tempMem.remove(tempMem1.indexOf(m));
				toVM(tempMem);
				return 1;
			}
		}
		return -1;
    }
    
    public int memLookup(int variableId) {

		//go through main mem
		//if not present then fromMem()
		//go through virtual mem, pop from arraylist 
		//if present swap into main mem and output
		//return swapped string toMem()

		for(int i = 0; i < memSize; i++){
			if(mainMemory[i] != null) {
				if(mainMemory[i].getID() == variableId){
					return mainMemory[i].getVal();
				}
			}
		}

		
		ArrayList<mem> tempMem1 = fromVM();
		ArrayList<mem> tempMem = tempMem1;
		int index;
		for (mem m: tempMem1) {
			if(m.getID() == variableId) { 
				if(counter < memSize) { //Free spot avaliable
					index = emptyFrame();
					mainMemory[index] = m; //insert lookup in MM
					tempMem.remove(tempMem1.indexOf(m)); //remove lookup from disk
					counter++;
					toVM(tempMem);
					return mainMemory[index].getVal();
				}
				else{ //must swap
					index = oldestPage();
					mem swapMem = new mem(mainMemory[index]);//stores page to be swapped
					mainMemory[index] = m;//overwrites page in MM
					tempMem.add(swapMem);//add faulted page to VM arraylist
					tempMem.remove(tempMem1.indexOf(m)); // remove lookup from disk
					toVM(tempMem);//writes back to disk
					isSwap = true;
					swapTime = (rand.nextInt(500));
					scheduler.pw.println("Clock: " + ( scheduler.time * 1000 - swapTime) + " Memory Manager, Swap: Variable " + swapMem.getID() +" with Variable " + m.getID());
					return mainMemory[index].getVal();
				}
			}
		}
	
		return -1;
	}
	
	//make a function to pull vm.txt into an mem array
	public ArrayList<mem> fromVM(){ //string converts into array of mem
		String[] fromDisk = readFile("vm.txt").split("\\s+");
		ArrayList<mem> mems = new ArrayList<mem>();
		for (int i = 0; i < fromDisk.length/3; i++){
			mems.add(new mem(
				Integer.parseInt(fromDisk[3 * i]),
				Integer.parseInt(fromDisk[3 * i + 1]),
				Integer.parseInt(fromDisk[3 * i + 2])));
		}
		return mems;
	}

	//make a funtion that puts mem array into vm.txt
	public void toVM (ArrayList<mem> toDisk){ //converts array of mem into string
		
		//split every 3 entry
		//3 entry per row
		//1st entry is Id
		//2nd entry is the value
		//3rd entry is the age

		try {
			PrintWriter pw = new PrintWriter(new File("vm.txt")); //erases old vm.txt
			for (mem m: toDisk){
				pw.println(m.getID() + " " + m.getVal() + " " + m.getAge());
			}

			pw.flush();
			pw.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		
	}

	
	
	//find the index of the oldestPage (only call if mainMemory is full)
	public int oldestPage(){ 
		int index = 0;
		for(int i = 1; i < memSize; i++){
			
			if(mainMemory[i].getAge() < mainMemory[i-1].getAge()) index = i;
			
		} 
		return index;
		// no avaliable slots
	}

	public int emptyFrame(){
		for(int i = 0; i < memSize; i++){
			if(mainMemory[i] == null){
				return i;
			}
		}
		return -1;
	}
	
	// if counter is even generate between 0-500
				// if counter is odd generate between 501-989 
				// for swap, always do tempTime - 10
	public int generateExecTime() {
    	if((commandCounter % 2) == 0) {
    		swapTime = 0;
    		return (rand.nextInt(499) + 500);
    	}
    	else {
    		if(isSwap) {
    			isSwap = false;
    			return swapTime -= 10;
    		}
    		else {
    			return rand.nextInt(500);
    		}
    		
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