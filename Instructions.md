Each bus_id is mapped to multiple pnr_id (one to many) that acts as an intermediate between passengers and bus.
The Kafka message does not contain a list of pnr_id in order to increase throughput and shorter JSON objects for scalability.
Using AWS SNS for the SMS/Notification Service.
Retry logic is implemented only after testing the APIs.
Populate 10 buses and 20 passengers for each bus with sample data.
500 buses and 25000 passengers.
Keep the code bare minimum.