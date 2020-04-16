import java.util.Comparator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

public class Proc extends Thread implements Comparable<Proc> {
    protected String name;
    protected int proc_id;
    protected int arrivalTime;
    protected int entryTime;
    protected int remainderTime;
    protected int quantumTime;
    protected int exitTime;
    protected Semaphore mutex = new Semaphore(0);
    protected static VMM vmm = new VMM();
    protected Semaphore procMutex = new Semaphore(0);

    public static void main(String args[]){
        Proc p = new Proc("P1",1, 1, 1);
        System.out.println(p);
        
        try { 
        	try {
        		p.mutex.release();
        	} finally {
        		p.mutex.acquire();
        		p.mutex.release();
        		p.mutex.acquire();
        		System.out.println("running");
        	}
        }
        catch(InterruptedException ie) {
            // ...
        }
    }

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
    /*
    public Proc(Proc p){
        this(p.name, p.id, p.arrivalTime, p.remainderTime);
    }
    */
    public void obtainCPU() {//to be changed
	    
        try {
            mutex.acquire();
            //System.out.println("here !");
            while( VMM.commandCounter < 2 ) { // race condition 
                try {
                    VMM.VMMMutex.acquire();
                } finally {
                	if(VMM.commandCounter < 2) { // cheap solution to fix race condition :) im poor
	                    vmm.API(this);
	                    VMM.procSync.acquire();
	                    VMM.commandCounter ++;
                	}
                	 VMM.VMMMutex.release();
                }
            }
            procMutex.release();
        } 
        catch(InterruptedException ie) {
                // ...
        }
	    
    }
    
    public void run() {
        while(remainderTime > 0) obtainCPU();
        try {
			this.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        // Break out of loop, thread termination
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