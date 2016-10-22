package nachos.threads;

import nachos.machine.*;
import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Uses the hardware timer to provide preemption, and to allow threads to sleep
 * until a certain time.
 */
public class Alarm {
    /**
     * Allocate a new Alarm. Set the machine's timer interrupt handler to this
     * alarm's callback.
     *
     * <p><b>Note</b>: Nachos will not function correctly with more than one
     * alarm.
     */
    public Alarm() {
	Machine.timer().setInterruptHandler(new Runnable() {
		public void run() { timerInterrupt(); }
	    });
    }

    /**
     * The timer interrupt handler. This is called by the machine's timer
     * periodically (approximately every 500 clock ticks). Causes the current
     * thread to yield, forcing a context switch if there is another thread
     * that should be run.
     */
    public void timerInterrupt() {
        long currentTime = Machine.timer().getTime();
        boolean intStatus = Machine.interrupt().disable();
        Iterator threadQueued = sleptThreads.iterator(); //thread en espera
        WakeUp wakeUpThread; //siguiente thread que se despierta
        while (threadQueued.hasNext()){
            wakeUpThread = (WakeUp)threadQueued.next();
            if(Machine.timer().getTime() >= wakeUpThread.getWakeUpTime()){
                wakeUpThread.getThread().ready();
                threadQueued.remove();
            }

        }
	    KThread.currentThread().yield();
    }

    /**
     * Put the current thread to sleep for at least <i>x</i> ticks,
     * waking it up in the timer interrupt handler. The thread must be
     * woken up (placed in the scheduler ready set) during the first timer
     * interrupt where
     *
     * <p><blockquote>
     * (current time) >= (WaitUntil called time)+(x)
     * </blockquote>
     *
     * @param   x   the minimum number of clock ticks to wait.
     *
     * @see nachos.machine.Timer#getTime()
     */
    private ArrayList<WakeUp> sleptThreads = new ArrayList();

    public void waitUntil(long x) {
        // for now, cheat just to get something working (busy waiting is bad)
        long wakeTime = Machine.timer().getTime() + x;
        KThread thread = KThread.currentThread();
        boolean intStatus = Machine.interrupt().disable();
        WakeUp wu = new WakeUp(thread,wakeTime);
        sleptThreads.add(wu);
        thread.sleep();
        Machine.interrupt().restore(intStatus);
        /*

        while (wakeTime > Machine.timer().getTime())
            KThread.yield();*/
    }

    public class WakeUp{
        KThread thread;
        long wakeUpTime;

        public WakeUp(KThread thread, long time){
            this.thread = thread;
            this.wakeUpTime = time;
        }
        public long getWakeUpTime(){
            return this.wakeUpTime;
        }
        public KThread getThread(){
            return this.thread;
        }


    }

}
