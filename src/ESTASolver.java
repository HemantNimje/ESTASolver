import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.util.PriorityQueue;

/**
 * Author: Hemant Nimje
 * File created on 11/28/2016.
 */
public class ESTASolver {
    static int timeLimit = 6000;
    static boolean goodWeather = true;
    static boolean badWeather = false;
    static boolean isChannel1Available = true;
    static boolean isChannel2Available = true;
    static boolean isChannel3Available = true;
    static int lostMessagesCount = 0;

    public static void main(String[] args) {
        ESTASolver solver = new ESTASolver();

        UniformRealDistribution URD = new UniformRealDistribution();
        double sample = URD.sample();
        //System.out.println(sample);

        // Titles for the FEL
        System.out.println("EventName" + "\t" + "EventStatus" + "\t" + "EventTime");

        // Create and insert Arrival events to the FEL
        PriorityQueue<Event> eventPriorityQueue = new PriorityQueue<Event>();
        Event eArrive = null;
        int eventOccurrenceTime = 0;
        int eventNumber = 1;
        while (eventOccurrenceTime <= timeLimit) {
            eArrive = new Event(eventNumber, "A", eventOccurrenceTime);
            eventNumber += 1;
            eventOccurrenceTime += 30;
            eventPriorityQueue.add(eArrive);
        }

        // Create and insert the Good Weather and Bad Weather event
        int currentTime = 0;
        while (currentTime < timeLimit) {
            if (goodWeather == true) {
                eventPriorityQueue.add(new Event(0, "GW", currentTime));
                currentTime += (int) new NormalDistribution(90, 10).sample();
                //eventPriorityQueue.add(new Event(0,"BW",currentTime));
                goodWeather = false;
                badWeather = true;
            } else if (badWeather == true) {
                eventPriorityQueue.add(new Event(0, "BW", currentTime));
                currentTime += ((int) new NormalDistribution(60, 20).sample());
                //eventPriorityQueue.add(new Event(0,"GW",currentTime));
                badWeather = false;
                goodWeather = true;
            }
        }

        // Create and insert Leave events to the FEL
        PriorityQueue<Event> FEL = new PriorityQueue<Event>();
        Event eDepart = null;
        int departTime = 0;
        goodWeather = true;
        badWeather = false;
        // Print the priority queue
        while (!eventPriorityQueue.isEmpty()) {

            if (eventPriorityQueue.peek().eventStatus == "GW") {
                //System.out.println("Good Weather");
                eventPriorityQueue.poll();
                goodWeather = true;
                badWeather = false;
            } else if (eventPriorityQueue.peek().eventStatus == "BW") {
                //System.out.println("Bad Weather");
                eventPriorityQueue.poll();
                badWeather = true;
                goodWeather = false;
            }
            Event currentEvent = eventPriorityQueue.poll();
            //System.out.println(currentEvent.getEventNumber() + "\t" + currentEvent.getEventStatus() + "\t" + currentEvent.getTime());

            if (currentEvent.eventStatus == "A") {
                if (isChannel1Available == false && isChannel2Available == false && isChannel3Available == false) {
                    lostMessagesCount += 1;
                    System.out.println();
                } else {
                    if (goodWeather == true) {
                        departTime += (new UniformRealDistribution().sample()) * 60;
                        eDepart = new Event(currentEvent.getEventNumber(), "D", departTime);
                        //System.out.println(currentEvent.getEventNumber() + "\t" + currentEvent.getEventStatus() + "\t" + currentEvent.getTime());
                        //System.out.println(eDepart.getEventNumber() + "\t" + eDepart.getEventStatus() + "\t" + eDepart.getTime());
                        eventPriorityQueue.add(eDepart);
                        FEL.add(currentEvent);
                        FEL.add(eDepart);

                        if (isChannel1Available == true){ isChannel1Available = false; }
                        else if (isChannel2Available == true){ isChannel2Available = false; }
                        else if (isChannel3Available == true){ isChannel3Available = false; }

                    } else if (badWeather == true) {
                        departTime += (Math.cbrt(new UniformRealDistribution().sample())) * 60;
                        eDepart = new Event(currentEvent.getEventNumber(), "D", departTime);
                        //System.out.println(currentEvent.getEventNumber() + "\t" + currentEvent.getEventStatus() + "\t" + currentEvent.getTime());
                        //System.out.println(eDepart.getEventNumber() + "\t" + eDepart.getEventStatus() + "\t" + eDepart.getTime());
                        eventPriorityQueue.add(eDepart);
                        FEL.add(currentEvent);
                        FEL.add(eDepart);

                        if (isChannel1Available == true){ isChannel1Available = false; }
                        else if (isChannel2Available == true){ isChannel2Available = false; }
                        else if (isChannel3Available == true){ isChannel3Available = false; }
                    }
                }
            }
            else if (currentEvent.eventStatus == "D"){
                if (isChannel1Available == false){ isChannel1Available = true; }
                else if (isChannel2Available == false){ isChannel2Available = true; }
                else if (isChannel3Available == false){ isChannel3Available = true; }
            }

            while (!FEL.isEmpty()){
                Event event = FEL.poll();
                System.out.println(event.getEventNumber() + "\t" + event.getEventStatus() + "\t" + event.getTime());
            }
        }
        System.out.println(lostMessagesCount);
    }
}
