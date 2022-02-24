//Driver is HERE!!!

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;
import java.util.PriorityQueue;
import java.util.ArrayList;

public class scheduler extends Thread{

	protected static final int numberOfCpu = 2;
 	protected static PriorityQueue<Proc> q1 = new PriorityQueue<>();// Priority queues to hold processes (FIFO)
 	protected static PrintWriter pw; //Create output.txt File
	protected static int time = 1; // Timer 
	protected static Proc[] finished; //store finished processes
	protected static int finishedCounter = 0; //counts finished processes
	protected static Proc[] inCpu = new Proc[numberOfCpu];
	protected static int inCpuCounter = 0;
	protected static int size;
	protected static ArrayList<Proc> procs;
	
    public static void main(String args[]) throws FileNotFoundException { //Driver class

    	 //Read file into a String Builder
        String[] number = readFile("input.txt").split("\\s+");
        int size = Integer.parseInt(number[0]);
        
        ArrayList<Proc> procs = new ArrayList<Proc>();
		for (int i = 0; i < size; i++){
            procs.add( new Proc(
				"P" + Integer.toString(i + 1), 
				i+1,
                Integer.parseInt(number[2 * i + 1]),  
                Integer.parseInt(number[2 * i + 2])));
		}
		
        
        VMM vmm = new VMM();
        scheduler s = new scheduler(size, procs); // Initialize scheduler to create two queues 
        
       pw.flush();
       pw.close();
       System.exit(0);
    }
        
    public scheduler(int s, ArrayList<Proc> p) {
    	
    	size = s;
    	procs = p;
    	
		for (Proc proc: procs) {
			q1.add(proc);
	    }
    	
    	try {
			pw = new PrintWriter(new File("./output.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	
    	start();
    	
    	try {
			join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    public void run() {
    	
    	Proc thisProc;
    	
    	while(finishedCounter != size) {
    		
    		
    		VMM.commandCounter = 0; //Reset commandCounter back to zero. (Ensure that only 2 commands are run in one iteration)

    		//if inCpu is not empty
    			//update all process in cpu : --remaindertime
    				//if any hits 0 then remove and put in finish array
    				//if any hits exitTime then put in ready queue (exitTime == time?)

    		if(inCpuCounter > 0){
    			
    			for(int i = 0; i < numberOfCpu; i++) {// wait until all processes are done
    				if(inCpu[i] != null){
    					try {
    						inCpu[i].procSync.acquire();
    					} catch (InterruptedException e) {
    						// TODO Auto-generated catch block
    						e.printStackTrace();
    					}
    				}
    			}
    			
    			for(int i = 0 ; i < numberOfCpu ; i++){ //Check if proc is done 
    				if(inCpu[i] != null){
    					inCpu[i].remainderTime -=  1;
    					if(inCpu[i].exitTime == time){
    						if(inCpu[i].remainderTime == 0){ // if done remove from queue
    							pw.println("Clock: "+time * 1000+", Process "+inCpu[i].name+", Finished");
    							finishedCounter ++;
    							inCpu[i] = null;
    							inCpuCounter--;
    						}
    						else{ // not done remove from queue
    							inCpu[i].setEntryTime(time);
    							q1.add(inCpu[i]);
    							pw.println("Clock: "+time * 1000+", Process "+inCpu[i].name+", Paused.");
    							inCpu[i] = null;
    							inCpuCounter--;
    						}
    					
    					}
    					else { // if exit time is not done release that process' semaphore
    						inCpu[i].schedulerSync.release();
    					}
    					
    				}
    			}
    		}
    		//if in cpu has empty slot
    			//check if we can add a process from the ready queue
    				//add the process
    					//define its exit time (now + quantum time)
    		
    		while(inCpuCounter < numberOfCpu && q1.peek() != null) {
    			//System.out.println("here");
    			if(q1.peek().getEntryTime() <= time){
    				//System.out.println("here too");
    				thisProc = q1.poll();
    				allocateCPU(thisProc);
    				inCpuCounter++;
    			}
    			else {
    				break;
    			}
    			
    		}
    		
    		time += 1;
    		
           }
    }
       
    public boolean allocateCPU(Proc p) {

		for(int i = 0 ; i < numberOfCpu ; i++){
			if(inCpu[i] == null){
				inCpu[i] = p;
				inCpu[i].setExitTime(time + inCpu[i].getQuantumTime());
				if(inCpu[i].getArrivalTime() == inCpu[i].getEntryTime()){
					pw.println("Clock: "+time * 1000+", Process "+inCpu[i].name+", Started.");
				}
				pw.println("Clock: "+time * 1000 +", Process "+inCpu[i].name+", Resumed.");
				//execute a command
				break;
			}
		}
	
		
		p.schedulerSync.release();
			
        
        return false;	
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


