//Driver is in Scheduler.java

import java.util.concurrent.Semaphore;

public class Proc extends Thread implements Comparable<Proc> {
	
    protected String name;
    protected int proc_id;
    protected int arrivalTime;
    protected int entryTime;
    protected int remainderTime;
    protected int quantumTime;
    protected int exitTime;
    protected Semaphore schedulerSync = new Semaphore(0);
    protected static Semaphore VMMMutex = new Semaphore(1);
    protected static VMM vmm = new VMM();
    protected Semaphore procSync = new Semaphore(0);

    public Proc (String n, int id, int arrT, int remT){
        name = n;
        proc_id = id;
        arrivalTime = arrT;
        entryTime = arrT;
        remainderTime = remT;
        if (remT*0.1 > 1) {
            quantumTime = (int) Math.ceil(remT*0.1);
        } 
        else {
            quantumTime = 1;
        }
		
      //Launch Thread
        this.start();
    }
  
    public void obtainCPU() {
	    
        try {
        	schedulerSync.acquire();
        	
            //System.out.println("here !");
            while( VMM.commandCounter < 2 ) { // race condition 
                try {
                    VMMMutex.acquire();
                } finally {
                	if(VMM.commandCounter < 2) { // cheap solution to fix race condition 
	                    VMM.procRunning = this;
	                    vmm.API(); // Run API
	                    VMM.procSync.acquire(); // wait until API is done
	                    
	                    VMM.commandCounter ++;
                	}
                	 VMMMutex.release(); //Tell other processes we are done with VMM
                }
            }
            procSync.release(); // Tell scheduler we are done
        } 
        catch(InterruptedException e) {
        	e.printStackTrace();
        }
	    
    }
    
    public void run() {
        while(remainderTime > 0) obtainCPU();
        
     // Break out of loop, thread termination
        try {
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public void setEntryTime(int input) {
        entryTime = input;
    }

    public void setRemainderTime(int input) {
        remainderTime = input;
        setQuantumTime();
    }

    public void setQuantumTime() {
        if (remainderTime*0.1 > 1) {
            double qT = (double)remainderTime * 0.1;
            quantumTime = (int)(Math.ceil(qT));
        } 
        else {
            quantumTime = 1;
        }
    }

    public void setExitTime(int input) {
        exitTime = input;
    }

    public String getProcessName() {
        return this.name;
    }

    public int getProcessID(){
        return this.proc_id;
    }

    public int getArrivalTime() {
        return this.arrivalTime;
    }

    public int getEntryTime() {
        return this.entryTime;
    }

    public int getRemainderTime() {
        return this.remainderTime;
    }

    public int getQuantumTime() {
        return this.quantumTime;
    }

    public int getExitTime(){
        return this.exitTime;
    }

    @Override
    public int compareTo(Proc x) {
        if (this.entryTime != x.getEntryTime()){
            return this.entryTime- x.getEntryTime();
        }
        else {
            return this.proc_id - x.getProcessID();
        }
    }

    
    @Override
    public String toString() {
        return String.format("Name: %s\n arrivalTime: %d\n remainderTime: %d\n exitTime: %d\n", 
            name, arrivalTime, remainderTime, exitTime);
    }
}