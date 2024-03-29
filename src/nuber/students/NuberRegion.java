package nuber.students;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * A single Nuber region that operates independently of other regions, other than getting 
 * drivers from bookings from the central dispatch.
 * 
 * A region has a maxSimultaneousJobs setting that defines the maximum number of bookings 
 * that can be active with a driver at any time. For passengers booked that exceed that 
 * active count, the booking is accepted, but must wait until a position is available, and 
 * a driver is available.
 * 
 * Bookings do NOT have to be completed in FIFO order.
 * 
 * @author james
 *
 */
public class NuberRegion {

	
	public String regionName;
	private NuberDispatch dispatch;
	private int maxSimultaneousJobs;
	
	//test
	private int totalCount = 0;
	int cores= Runtime.getRuntime().availableProcessors(); //determine the cpu core 
	private Executor executor;
	
	
	/**
	 * Creates a new Nuber region
	 * 
	 * @param dispatch The central dispatch to use for obtaining drivers, and logging events
	 * @param regionName The regions name, unique for the dispatch instance
	 * @param maxSimultaneousJobs The maximum number of simultaneous bookings the region is allowed to process
	 */
	public NuberRegion(NuberDispatch dispatch, String regionName, int maxSimultaneousJobs)
	{
		this.dispatch = dispatch;
		this.regionName = regionName;
		this.maxSimultaneousJobs = maxSimultaneousJobs;
		this.executor= Executors.newFixedThreadPool(cores);
	}
	
	/**
	 * Creates a booking for given passenger, and adds the booking to the 
	 * collection of jobs to process. Once the region has a position available, and a driver is available, 
	 * the booking should commence automatically. 
	 * 
	 * If the region has been told to shutdown, this function should return null, and log a message to the 
	 * console that the booking was rejected.
	 * 
	 * @param waitingPassenger
	 * @return a Future that will provide the final BookingResult object from the completed booking
	 */
	public Future<BookingResult> bookPassenger(Passenger waitingPassenger)
	{
		//testing it all out might have to make changes  
		CompletableFuture<BookingResult> future = new CompletableFuture<>();
		// thread using a lambda expression 
		Thread bookingThread = new Thread(() -> {
			try {
				Booking booking = new Booking(dispatch,waitingPassenger);
				BookingResult result = booking.call();
				future.complete(result);
				dispatch.completeBooking();
		}catch(Exception e)
		{
			future.completeExceptionally(e);
		}
	});
		bookingThread.start();
		return future;			//testing it all out 
	}
	
	/**
	 * Called by dispatch to tell the region to complete its existing bookings and stop accepting any new bookings
	 */
	public synchronized void shutdown()
	{
		((ExecutorService) executor).shutdown(); //executor.shutdown does not get implemented but rather putting a cast around it does the work 
												// need to try another way as well on why's thats the case
		
	}

	public int pendingBookings() {
		// TODO Auto-generated method stub
		return totalCount;
	}
		
}
