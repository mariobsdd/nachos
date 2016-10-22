package nachos.threads;

import nachos.machine.*;

/**
 * A <i>communicator</i> allows threads to synchronously exchange 32-bit
 * messages. Multiple threads can be waiting to <i>speak</i>,
 * and multiple threads can be waiting to <i>listen</i>. But there should never
 * be a time when both a speaker and a listener are waiting, because the two
 * threads can be paired off at this point.
 */

//


public class Communicator {
    /**
     * Allocate a new communicator.*/
	public Lock lock;
	public Condition2 listener; //ok to listen
	public Condition2 speaker; //ok to speak
	public int msj = 0; //mensaje del speaker
	public boolean canSpeak = true; //speakers activo
	private static final char dbgThread = 'c';
	public int wl = 0;

    public Communicator() {
    	//instancio 
    	lock = new Lock();
    	listener = new Condition2(lock);
    	speaker = new Condition2(lock);
    }

    /**
     * Wait for a thread to listen through this communicator, and then transfer
     * <i>word</i> to the listener.
     *
     * <p>
     * Does not return until this thread is paired up with a listening thread.
     * Exactly one listener should receive <i>word</i>.
     *
     * @param	word	the integer to transfer.
     */
    public void speak(int word) {
    	
    	lock.acquire();
    	while(!canSpeak){
    		speaker.sleep();
    	}
    	
    	lock.release();
    	//el thread lee el mensaje
    	System.out.println("El thread speaks: " + KThread.currentThread().getName() + " "+this.msj);
    	this.msj = word;
    	
    	lock.acquire();
    	canSpeak = false;


    	if(canSpeak){
    		speaker.wake();
    	}else{
    		//despiertar a todos los listeners
    		listener.wakeAll();
    	}

    	lock.release();
    }

    /**
     * Wait for a thread to speak through this communicator, and then return
     * the <i>word</i> that thread passed to <tt>speak()</tt>.
     *
     * @return	the integer transferred.
     */    
    public int listen() {
    	Lib.debug(dbgThread, "entra al listener: ");
    	lock.acquire();
    	while(canSpeak){
    		//wl++;
    		listener.sleep();
    		//wl--;
    	}
    	//aqui ya desperto un listener
    	//alguien esta hablando
    	lock.release();
    	//Lib.debug(dbgThread, "el thread: "+KThread.currentThread().getName() + " leyo el mensaje: "+" "+this.msj);
    	System.out.println("El thread : "+KThread.currentThread().getName() + " leyo el mensaje: "+" "+this.msj);
    	lock.acquire();
    	canSpeak = true;
    	speaker.wake();
    	lock.release();

		return this.msj;

    }
}
