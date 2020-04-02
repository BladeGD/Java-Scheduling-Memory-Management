import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.File;
import java.util.PriorityQueue;
import java.util.concurrent.Semaphore;

public class scheduler extends Thread {
	
 	protected static PriorityQueue<Proc> q1 = new PriorityQueue<>();// Priority queues to hold processes
 	protected static PriorityQueue<Proc> q2 = new PriorityQueue<>();
 	protected static PrintWriter pw; //Create output.txt File
	protected static int time = 1; // Timer 
	protected Proc[] finished; //store finished processes
	protected static int finishedCounter = 0; //counts finished processes
	protected static Semaphore schedulerMutex = new Semaphore(1);
    protected static Proc[] inCpu = new Proc[2];
    protected static int lastestInCpu = 0;
    
	
    
    public static void main(String args[]) throws FileNotFoundException {

    	 //Read file into a String Builder
        String[] number = readFile("input.txt").split("\\s+");
        int size = number.length/2;
        
        scheduler s = new scheduler(size); // Initialize scheduler to create two queues and mutex 

        //Convert String Builder to Proc Objects
        Proc[] procs = new Proc[number.length/2];
        for (int i = 0; i < procs.length; i++){
            procs[i] = new Proc(
                "P"+ Integer.toString(i+1), 
                Integer.parseInt(number[2 * i]), 
                Integer.parseInt(number[2 * i]), 
                Integer.parseInt(number[2 * i + 1]), 
                Integer.parseInt(number[2 * i + 1]), 
                false, 
                false);
        }
        
        
        Proc thisProc;
        
        while(procs[0].getArrivalTime() < time) { //Fast forward to first arrival time
    		time++;
    	}

        for (Proc proc: procs) {
        	
        	while(proc.arrivalTime > time) { //Fast forward to next arrival time, wasting CPU cycles.....
        		if(q1.peek() != null) {
        			Proc tempProc = q1.poll();
        			if(!s.allocateCPU(tempProc)) {
        				q2.add(tempProc);
        				pw.println("Time "+time+", Process "+proc.name+", Paused");
        			}
        		}
        		else if(q2.peek() != null) {
        			Proc tempProc = q1.poll();
        			if(!s.allocateCPU(tempProc)) {
        				q1.add(tempProc);
        				pw.println("Time "+time+", Process "+proc.name+", Paused");
        			}
        		}
        		else {
        			time = proc.arrivalTime;
        		}
        	}
        	
        	pw.println("Time "+time+", Process "+proc.name+", Started");
            
            if(!s.allocateCPU(proc)) {
            	q1.add(proc);
                pw.println("Time "+time+", Process "+proc.name+", Paused");
            }
        }
        

       while(finishedCounter != size) {

            while(q1.peek() != null) {
            	thisProc = q1.poll();
            	if(!s.allocateCPU(thisProc)) {
            		q2.add(thisProc);
                    pw.println("Time "+time+", Process "+thisProc.name+", Paused");
            	}
            }
            
            while(q2.peek() != null) {
            	thisProc = q2.poll();
            	if(!s.allocateCPU(thisProc)) {
            		q1.add(thisProc);
                    pw.println("Time "+time+", Process "+thisProc.name+", Paused");
            	}
            }
        }
       
       pw.println("------------------------------");
       pw.println("Waiting Times:");
       
       for(Proc proc : s.finished) {
    	   pw.println("Process " + proc.name +": " + proc.waitTime);
       }

        //Write to File and close streams
        pw.flush();
        pw.close();
    }
        
    public scheduler(int size) {
    	finished = new Proc[size];
    	try {
			pw = new PrintWriter(new File("./output.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
    	start();
    }

    public void run() {}
       
    public boolean allocateCPU(Proc p) {
    	
    	if(isProcessFinished(p)) {
    		return true;
    	}
    	
    	pw.println("Time "+time+", Process "+p.name+", Resumed ," + "remaining time" + p.remainderTime);
    	time += p.getQuantumTime();
    	
    	
    	p.mutex.release();
    	
        try {
           scheduler.schedulerMutex.acquire();  // wait until process is done
        }    
        catch(InterruptedException ie) {
                // ...
        }
        
        return false;
		
	}
    
	public boolean isProcessFinished(Proc p) {
		
    	if(p.getRemainderTime() == 0) {
	    	finished[finishedCounter] = p;
	        finishedCounter++;
	        p.waitTime = time - p.burstTime - p.arrivalTime;
	        pw.println("Time "+time+", Process "+p.name+", Paused");
	        pw.println("Time "+time+", Process "+p.name+", Finished");
	        return true;
    	}
    	
    	return false;
    }
	
	public void pauseToAdd(Proc p) {
		pw.println("Time "+time+", Process "+inCpu[lastest].name+", Paused");
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


