package nuber.students;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

/**
 * The core Dispatch class that instantiates and manages everything for Nuber
 * 
 * @author james
 *
 */
public class NuberDispatch {

	/**
	 * The maximum number of idle drivers that can be awaiting a booking 
	 */
	private final int MAX_DRIVERS = 999;
	
	private boolean logEvents = false;

	private HashMap<String, Integer> regionInfo;
	
	private Map<String, NuberRegion> regions = new HashMap<>();
	
	//testing this
//	private Queue <Driver> inactiveDrivers = new LinkedList<>();
	
	private BlockingQueue<Driver> inactiveDrivers;
	
	private int totalBookings = 0;
	
	private int totalPendingBookings = 0;

	private boolean shutdown = false;
	
	
	
	/**
	 * Creates a new dispatch objects and instantiates the required regions and any other objects required.
	 * It should be able to handle a variable number of regions based on the HashMap provided.
	 * 
	 * @param regionInfo Map of region names and the max simultaneous bookings they can handle
	 * @param logEvents Whether logEvent should print out events passed to it
	 */
	public NuberDispatch(HashMap<String, Integer> regionInfo, boolean logEvents)
	{
		this.logEvents = logEvents;
		this.regionInfo = regionInfo;
		
		this.inactiveDrivers = new ArrayBlockingQueue<>(MAX_DRIVERS);
		
		 for (Map.Entry<String, Integer> entry : regionInfo.entrySet()) {
	            String regionName = entry.getKey();
	            int maxSimultaneousJobs = entry.getValue();
	            NuberRegion region = new NuberRegion(this, regionName, maxSimultaneousJobs);
	            regions.put(regionName, region);
	        }
	}
	
	
	
	/**
	 * Adds drivers to a queue of idle driver.
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @param The driver to add to the queue.
	 * @return Returns true if driver was added to the queue
	 */
	public synchronized boolean addDriver(Driver newDriver)
	{
		
		if (inactiveDrivers.offer(newDriver)) {
			notifyAll() ;
			
			
			return true;
		}
		
		return false;
	}
		
	
	
	/**
	 * Gets a driver from the front of the queue
	 *  
	 * Must be able to have drivers added from multiple threads.
	 * 
	 * @return A driver that has been removed from the queue
	 */
	public synchronized Driver getDriver() throws InterruptedException
	{
		while (inactiveDrivers.isEmpty()) {
			
			wait();		
		}
		notifyAll();
		return inactiveDrivers.poll();
	}

	/**
	 * Prints out the string
	 * 	    booking + ": " + message
	 * to the standard output only if the logEvents variable passed into the constructor was true
	 * 
	 * @param booking The booking that's responsible for the event occurring
	 * @param message The message to show
	 */
	public void logEvent(Booking booking, String message) {
		
		if (!logEvents) return;
		
		System.out.println(booking + ": " + message);
		
	}

	/**
	 * Books a given passenger into a given Nuber region.
	 * 
	 * Once a passenger is booked, the getBookingsAwaitingDriver() should be returning one higher.
	 * 
	 * If the region has been asked to shutdown, the booking should be rejected, and null returned.
	 * 
	 * @param passenger The passenger to book
	 * @param region The region to book them into
	 * @return returns a Future<BookingResult> object
	 */
	public Future<BookingResult> bookPassenger(Passenger passenger, String region) {
		//return regions.get(region).bookPassenger(passenger);
	//}
		
		  if (shutdown) {
	            return null; // should Return null if the dispatch has been shut down
	        }

	        NuberRegion nuberRegion = regions.get(region);
	        if (nuberRegion != null) {
	            Future<BookingResult> result = nuberRegion.bookPassenger(passenger);
	            if (result != null) {
	                totalPendingBookings++;
	            }
	            return result;
	        } else {
	            // Handle the case where the region doesn't exist
	            return null;
	        }
	}
		
		

	//test
	public synchronized void completeBooking() {
		if(totalPendingBookings > 0) {
		totalPendingBookings --;
		}
	}

	/**
	 * Gets the number of non-completed bookings that are awaiting a driver from dispatch
	 * 
	 * Once a driver is given to a booking, the value in this counter should be reduced by one
	 * 
	 * @return Number of bookings awaiting driver, across ALL regions
	 */
	public int getBookingsAwaitingDriver()
	{
		
		int total = 0;
		for(NuberRegion region: regions.values()) {
			total += region.pendingBookings();
		}
		
		return total;
	}
	

	/**
	 * Tells all regions to finish existing bookings already allocated, and stop accepting new bookings
	 */
	public void shutdown() {
		shutdown = true;
        for (NuberRegion region : regions.values()) {
            region.shutdown();
        }
    }		
		/*
		for (NuberRegion region : regions.values()) {
			region.shutdown();
		}
		*/
}
