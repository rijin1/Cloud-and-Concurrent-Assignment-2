package nuber.students;

import java.util.concurrent.Callable;

/**
 * 
 * Booking represents the overall "job" for a passenger getting to their destination.
 * 
 * It begins with a passenger, and when the booking is commenced by the region 
 * responsible for it, an available driver is allocated from dispatch. If no driver is 
 * available, the booking must wait until one is. When the passenger arrives at the destination,
 * a BookingResult object is provided with the overall information for the booking.
 * 
 * The Booking must track how long it takes, from the instant it is created, to when the 
 * passenger arrives at their destination. This should be done using Date class' getTime().
 * 
 * Booking's should have a globally unique, sequential ID, allocated on their creation. 
 * This should be multi-thread friendly, allowing bookings to be created from different threads.
 * 
 * @author james
 *
 */
public class Booking implements Callable<BookingResult> {

		
	
	private NuberDispatch dispatch;
	private Passenger passenger;
	private Driver driver;
	private int bokingID;
	private static int ID;
	
	/**
	 * Creates a new booking for a given Nuber dispatch and passenger, noting that no
	 * driver is provided as it will depend on whether one is available when the region 
	 * can begin processing this booking.
	 * 
	 * @param dispatch
	 * @param passenger
	 */
	public Booking(NuberDispatch dispatch, Passenger passenger)
	{
		this.dispatch = dispatch;
		this.passenger = passenger;
		
		synchronized(Booking.class) {
		this.bokingID = ID++;
		}
	}
	
	//testing it 

	
	/**
	 * At some point, the Nuber Region responsible for the booking can start it (has free spot),
	 * and calls the Booking.call() function, which:
	 * 1.	Asks Dispatch for an available driver
	 * 2.	If no driver is currently available, the booking must wait until one is available. 
	 * 3.	Once it has a driver, it must call the Driver.pickUpPassenger() function, with the 
	 * 			thread pausing whilst as function is called.
	 * 4.	It must then call the Driver.driveToDestination() function, with the thread pausing 
	 * 			whilst as function is called.
	 * 5.	Once at the destination, the time is recorded, so we know the total trip duration. 
	 * 6.	The driver, now free, is added back into Dispatch�s list of available drivers. 
	 * 7.	The call() function the returns a BookingResult object, passing in the appropriate 
	 * 			information required in the BookingResult constructor.
	 *
	 * @return A BookingResult containing the final information about the booking 
	 */
	public BookingResult call() {
		try {
				Driver driver = dispatch.getDriver();
				
				if(driver != null) {
					driver.pickUpPassenger(passenger);
					
					Thread.sleep(100);
					
				driver.driveToDestination();	
				
				Thread.sleep(100);
			

	           long startTime = System.currentTimeMillis();
	            // Assuming that driving to the destination and recording the time takes some time

	            // Simulate a delay for the driver to reach the destination
	            Thread.sleep(2000); // Adjust the duration as needed

	            // Record the end time
	            long endTime = System.currentTimeMillis();

	            // Calculate the total trip duration
	            long tripDuration = endTime - startTime;

	            // Step 6: Add the driver back into Dispatch's list of available drivers
	            dispatch.addDriver(driver);

	            // Step 7: Return a BookingResult with appropriate information
	            return new BookingResult(ID, passenger, driver, tripDuration);
	        } else {
	            // No available driver
	            return new BookingResult(ID, passenger, null, 0);
	        }
		} catch (InterruptedException e) {
	        Thread.currentThread().interrupt();
	        return null; // Handle the exception as needed
	    }

	}
	
	/***
	 * Should return the:
	 * - booking ID, 
	 * - followed by a colon, 
	 * - followed by the driver's name (if the driver is null, it should show the word "null")
	 * - followed by a colon, 
	 * - followed by the passenger's name (if the passenger is null, it should show the word "null")
	 * 
	 * @return The compiled string
	 */
	@Override
	public String toString()
	{
		String driverName = (driver != null) ? driver.getName() : "null";
	    String passengerName = (passenger != null) ? passenger.getName() : "null";
	    return String.format("%d:%s:%s", bokingID, driverName, passengerName);
	}
		
	}


