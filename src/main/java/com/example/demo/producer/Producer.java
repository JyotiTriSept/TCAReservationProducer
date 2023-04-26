package com.example.demo.producer;

import java.util.List;

import org.springframework.stereotype.Component;

import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubClientBuilder;
import com.azure.messaging.eventhubs.EventHubProducerClient;

@Component
public class Producer {
    
	public String publishEvents(List<EventData> eventDataList) {
		// create a producer client
				EventHubProducerClient producer = new EventHubClientBuilder()
						.connectionString("Endpoint=sb://alg-ehub-ns.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=+fcEzuI7B9sFRFuxR0wtK4IxYU00RmH0hfFga9K+msA=", "tcs-reserv-ehub-poc")
						.buildProducerClient();
				
				System.out.println(Thread.currentThread().getName()+" is sending event to EventHub: " + "tcs-reserv-ehub-poc");
				// create a batch
		        EventDataBatch eventDataBatch = producer.createBatch();

		        for (EventData eventData : eventDataList) {
		            // try to add the event from the array to the batch
		            if (!eventDataBatch.tryAdd(eventData)) {
		                // if the batch is full, send it and then create a new batch
		                producer.send(eventDataBatch);
		                eventDataBatch = producer.createBatch();

		                // Try to add that event that couldn't fit before.
		                if (!eventDataBatch.tryAdd(eventData)) {
		                    throw new IllegalArgumentException("Event is too large for an empty batch. Max size: "
		                        + eventDataBatch.getMaxSizeInBytes());
		                }
		            }
		        }
		        // send the last batch of remaining events
		        if (eventDataBatch.getCount() > 0) {
		            producer.send(eventDataBatch);
		        }
		        producer.close();
				
				return "Data sent to Event Hub by "+Thread.currentThread().getName();
	}
}
