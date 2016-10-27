package nachos.threads;
import nachos.ag.BoatGrader;

public class Boat
{
    

    public static Lock lock = new Lock();
	public static boolean boatOnOahu = true; //para saber donde esta el bote

	//condition variables
	public static Condition ubicacionBote = new Condition(lock);
	public static Condition oahuCond = new Condition(lock);
	public static Condition molokaiCond = new Condition(lock);

	public static Isla Oahu = new Isla(0,0, oahuCond);
	public static Isla Molokai = new Isla(0,0, molokaiCond);

	//asientos disponibles en el bote. 2 ninos top, 1 adult top
	public static int seatsAvailable = 2;
    
    static BoatGrader bg;
    
    public static void selfTest()
    {
		BoatGrader b = new BoatGrader();

		
		System.out.println("\n ***Testing Boats with only 2 children***");
		begin(2, 2, b);

	//	System.out.println("\n ***Testing Boats with 2 children, 1 adult***");
	//  	begin(1, 2, b);

	//  	System.out.println("\n ***Testing Boats with 3 children, 3 adults***");
	//  	begin(3, 3, b);
    }

    public static void begin( int adults, int children, BoatGrader b )
    {
		// Store the externally generated autograder in a class
		// variable to be accessible by children.
		bg = b;
		Oahu.setAdult(adults);
		Oahu.setChild(children);

		// Instantiate global variables here
		
		// Create threads here. See section 3.4 of the Nachos for Java
		// Walkthrough linked from the projects page.

		Runnable adult = new Runnable() {
		    public void run() {
	                AdultItinerary();
            }
        };
        Runnable child = new Runnable() {
		    public void run() {
	                ChildItinerary();
            }
        };
	    
	    for (int i=0; i<adults ; i++) {
	    	KThread t = new KThread(adult);
	    	t.setName("Adulto #"+i);
	    	t.fork();
	    	//t.join();
	    }
	    for (int i=0; i<children ; i++) {
	    	KThread t = new KThread(child);
	    	t.setName("CHILDREN #"+i);
	    	t.fork();
	    	//t.join();
	    }

    }

    static void AdultItinerary()
    {
		/* This is where you should put your solutions. Make calls
		   to the BoatGrader to show that it is synchronized. For
		   example:
		       bg.AdultRowToMolokai();
		   indicates that an adult has rowed the boat across to Molokai
		*/

		 //EL ADULTO OCUPA DOS ESPACIOS. NO PUEDE VIAJAR CON UN CHILD
		  //DEBE DE VIAJAR SOLO

		boolean whichIsland = true; //para saber en que isla estoy. TRUE si estoy en Oahu
		lock.acquire();
		while(true){
			if (whichIsland){
				if(boatOnOahu){
					if(!(seatsAvailable > 1)){
						Oahu.getIsla().sleep();
					}
					else if (Oahu.getChild() >=2){
						Oahu.getIsla().sleep();
					}
					else{ //cuando ya se puede subir al barco
						whichIsland = false;
						seatsAvailable = 0;
						Oahu.decrementAdult();
						bg.AdultRowToMolokai(); //rema
						Molokai.incrementAdult();
						Molokai.setPeople(Oahu.getPopulation()); //a molokai le pongo la ponlacion de oahu
						seatsAvailable = 2; 
						boatOnOahu = false;
						Molokai.getIsla().wakeAll();
						Molokai.getIsla().sleep();
					}
				}
				else{
					Oahu.getIsla().sleep();
				}
				
			}
			else{
				Molokai.getIsla().sleep();
			}
		}

    }

    static void ChildItinerary()
    {
    	boolean whichIsland = true;
    	lock.acquire();

    	while(true){
    		if (whichIsland){
    			if(boatOnOahu){
    				if(Oahu.getChild() <2){
	    				Oahu.getIsla().sleep();
	    			}

	    			if(seatsAvailable > 1){
	    				whichIsland = false;
	    				Oahu.decrementChild();
	    				seatsAvailable = 1;
	    				bg.ChildRowToMolokai();
	    				
	    				if(Oahu.getChild() > 0){
	    					Oahu.incrementChild();
	    					Oahu.getIsla().wakeAll();
	    				}
	    				else{
	    					boatOnOahu = false;
	    					seatsAvailable = 2;
	    					Molokai.incrementChild();
	    					Molokai.setPeople(Oahu.getPopulation());
	    					Molokai.getIsla().wakeAll();
	    				}
	    				Molokai.getIsla().sleep();
	    			}
	    			else if(seatsAvailable == 1){
	    				seatsAvailable = 0;
	    				Oahu.decrementChild();
	    				Oahu.decrementChild();
	    				bg.ChildRideToMolokai();
	    				Molokai.incrementChild();
	    				Molokai.incrementChild();
	    				Molokai.setPeople(Oahu.getPopulation());
	    				seatsAvailable = 2;
	    				whichIsland = false;
	    				boatOnOahu = false;
	    				Molokai.getIsla().wakeAll();
	    				Molokai.getIsla().sleep();
	    			}
	    			else{
	    				Oahu.getIsla().sleep();
	    			}
    			}
    			else{
    				Oahu.getIsla().sleep();	
    			}
    		}
    		else{
    			if(Molokai.getPeople() == 0){
    				System.out.println("yackson");
    				Molokai.getIsla().sleep();
    			}
    			else{
    				if(!boatOnOahu){
    					whichIsland = true;
    					Molokai.decrementChild();
    					seatsAvailable = 1;
    					bg.ChildRowToOahu();
    					Oahu.incrementChild();
    					Oahu.setPeople(Molokai.getPopulation());
    					seatsAvailable = 2;
    					boatOnOahu = true;
    					Oahu.getIsla().wakeAll();
    					Oahu.getIsla().sleep();
    				}
    				else{
    					Molokai.getIsla().sleep();
    				}
    			}
    		}
    	}

    }

    static void SampleItinerary()
    {
		// Please note that this isn't a valid solution (you can't fit
		// all of them on the boat). Please also note that you may not
		// have a single thread calculate a solution and then just play
		// it back at the autograder -- you will be caught.
		System.out.println("\n ***Everyone piles on the boat and goes to Molokai***");
		bg.AdultRowToMolokai();
		bg.ChildRideToMolokai();
		bg.AdultRideToMolokai();
		bg.ChildRideToMolokai();
    }

        //CLASE ISLA
    public static class Isla{
    	
    	public int child, adult;
    	public Condition isla;
    	//numero de personas en la otra isla
    	public int people;

    	public Isla(int child, int adult, Condition isla){
    		this.child = child;
    		this.adult = adult;
    		this.isla = isla;
    	}

    	//sets y gets
    	public int  getChild(){
    		return child;
    	}
    	public int getAdult(){
    		return adult;
    	}
    	public Condition getIsla(){
    		return isla;
    	}
    	public int getPeople(){
    		return people;
    	}
    	public void setChild(int child){
    		this.child = child;
    	}
    	public void setAdult(int adult){
    		this.adult = adult;
    	}
    	public void setIsla(Condition isla){
    		this.isla = isla;
    	}
    	public void setPeople(int people){
    		this.people = people;
    	}


    	//para poder incrementar y decrementar los ninos y adultos
    	public void incrementChild(){
    		child = child + 1;
    	}
    	public void decrementChild(){
    		child = child - 1;
    	}
    	public void incrementAdult(){
    		adult = adult + 1;
    	}
    	public void decrementAdult(){
    		adult = adult - 1;
    	}

    	//devuelve la suma de ninos y adultos en una isla
    	public int getPopulation(){
    		return child + adult;
    	}

    }

}
