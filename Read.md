This is a project to automate calls for the bus passengers based on the current location or time between the bus and the passenger.
It is purely backend with Java Spring Boot. I will be receiving the location of the bus in Kafka and calculate the time taken to reach the specified passenger location (set while booking) based on the Google Maps time or any other distance and time calculator. I have to send the notification message and call all the passengers at the pickup point.
The pickup points will be stored in bus_passenger table. The location and id of the bus will be received through Kafka. The code will be bare minimal to serve the functionality.
This should serve 500 buses and 2500 passengers.
I am not worried about the booking seats.


