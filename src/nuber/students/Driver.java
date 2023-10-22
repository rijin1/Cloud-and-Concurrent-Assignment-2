 package nuber.students;

public class Driver extends Person {
	
	private Passenger ongoingPassenger;
	//private string driverName;
	
	public Driver(String driverName, int maxSleep)
	{
		super(driverName, maxSleep);
		
	}
	
	/**
	 * Stores the provided passenger as the driver's current passenger and then
	 * sleeps the thread for between 0-maxDelay milliseconds.
	 * 
	 * @param newPassenger Passenger to collect
	 * @throws InterruptedException
	 */
	public void pickUpPassenger(Passenger newPassenger) throws InterruptedException 
	{
		ongoingPassenger=newPassenger;
		int sleep=(int) (Math.random()*(getMaxSleep()+1));
		Thread.sleep(sleep);
	}

	/**
	 * Sleeps the thread for the amount of time returned by the current 
	 * passenger's getTravelTime() function
	 * 
	 * @throws InterruptedException
	 */
	public void driveToDestination() {
	}
	
}
