import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;

import java.io.IOException;
import java.util.PriorityQueue;

/**
 * Author: Hemant Nimje
 * File created on 11/28/2016.
 */
public class ESTASolver {
    private static int timeLimit = 6000;
    private static boolean goodWeather = true;
    private static boolean badWeather = false;
    private static boolean isChannel1Available = true;
    private static boolean isChannel2Available = true;
    private static boolean isChannel3Available = true;
    private static int lostMessagesCount = 0;
    private static int avgLostMessagesCount = 0;
    private static int x = 0;
    private static int n = 10000;
    private static int sum = 0;

    public static void main(String args[]) throws IOException {
        ESTASolver solver = new ESTASolver();

        /*lostMessagesCount = solver.getLostMessagesCount();
        // Print the number of messages that are lost
        System.out.println("Number of msg lost: " + lostMessagesCount);
*/
        //avgLostMessagesCount = solver.getAvgLostMessagesCount();
        // Print the average number of messages that are lost
        //System.out.println("Average number of lost messages: " + avgLostMessagesCount);


        for (int i = 0; i < n; i++) {
            lostMessagesCount = solver.getLostMessagesCount();
            //x = solver.getLostMessagesCount();
            //sum = sum + lostMessages;
        }
        avgLostMessagesCount = lostMessagesCount / n;
        
        System.out.println("Average number of lost messages: " + avgLostMessagesCount);

    }

    private int getLostMessagesCount() {

        // Titles for the FEL
        //System.out.println("EventName" + "\t" + "EventStatus" + "\t" + "EventTime");

        // Create priority queue to maintain future event list
        PriorityQueue<Event> eventPriorityQueue = new PriorityQueue<>();
        int eventOccurrenceTime = 0;
        int eventNumber = 1;

        // Create and insert Arrival events to the FEL
        while (eventOccurrenceTime <= timeLimit) {

            double U = new UniformRealDistribution().sample();
            double x = ((Math.log(1 / (1 - U))) / 2) * 60;
            eventOccurrenceTime += (int) x;

            if (eventOccurrenceTime <= timeLimit) {
                Event eArrive = new Event(eventNumber, "A", eventOccurrenceTime);
                eventNumber += 1;
                eventPriorityQueue.add(eArrive);
            }
        }

        // Print priority queue
       /* while (!eventPriorityQueue.isEmpty()) {
            Event currentEvent = eventPriorityQueue.poll();
            System.out.println(currentEvent.getEventNumber() + "\t" + currentEvent.getEventStatus() + "\t"
                    + currentEvent.getTime());
        }
*/
        // Create and insert the Good Weather and Bad Weather event
        int currentTime = 0;


        // Till the time reach limit of 100 hours i.e 6000 minutes and add the good weather and bad weather which will
        // be required for further operations on priority queue
        while (currentTime < timeLimit) {
            if (goodWeather) {
                eventPriorityQueue.add(new Event(0, "GW", currentTime));
                currentTime += (int) new NormalDistribution(90, 10).sample();
                //eventPriorityQueue.add(new Event(0,"BW",currentTime));
                goodWeather = false;
                badWeather = true;
            } else if (badWeather) {
                eventPriorityQueue.add(new Event(0, "BW", currentTime));
                currentTime += ((int) new NormalDistribution(60, 20).sample());
                //eventPriorityQueue.add(new Event(0,"GW",currentTime));
                badWeather = false;
                goodWeather = true;
            }
        }

        // FEL - Future event list to keep track of the future events of arrival and departure
        PriorityQueue<Event> FEL = new PriorityQueue<>();
        int departTime = 0;
        goodWeather = true;
        badWeather = false;

        // Create and insert Leave events along with the arrive events to the FEL
        while (!eventPriorityQueue.isEmpty()) {

            //pre-process the weather condition for further calculation of event processing time
            if (eventPriorityQueue.peek().eventStatus.equals("GW")) {
                //System.out.println("Good Weather");
                eventPriorityQueue.poll();
                goodWeather = true;
                badWeather = false;
            } else if (eventPriorityQueue.peek().eventStatus.equals("BW")) {
                //System.out.println("Bad Weather");
                eventPriorityQueue.poll();
                badWeather = true;
                goodWeather = false;
            } else {

                // get the top event from the priority queue
                Event currentEvent = eventPriorityQueue.poll();


                // If the event is arrival then check whether any channel is available or not.
                // If channel is available then calculate the processing time depending on the good weather or bad weather
                // condition.
                if (currentEvent.eventStatus.equals("A")) {
                    if (!isChannel1Available && !isChannel2Available && !isChannel3Available) {
                        lostMessagesCount += 1;
                        //System.out.println("Event " + currentEvent.eventNumber + " is lost");
                    } else {
                        if (goodWeather) {
                            departTime += (new UniformRealDistribution().sample()) * 60;
                            Event eDepart = new Event(currentEvent.getEventNumber(), "D", departTime);

                            //print current event and its departure event
                        /*System.out.println(currentEvent.getEventNumber() + "\t" + currentEvent.getEventStatus() + "\t"
                                + currentEvent.getTime());
                        System.out.println(eDepart.getEventNumber() + "\t" + eDepart.getEventStatus() + "\t"
                                + eDepart.getTime());
*/
                            eventPriorityQueue.add(eDepart);
                            FEL.add(currentEvent);
                            FEL.add(eDepart);

                            if (isChannel1Available) {
                                isChannel1Available = false;
                            } else if (isChannel2Available) {
                                isChannel2Available = false;
                            } else if (isChannel3Available) {
                                isChannel3Available = false;
                            }

                        } else if (badWeather) {
                            departTime += (Math.cbrt(new UniformRealDistribution().sample())) * 60;
                            Event eDepart = new Event(currentEvent.getEventNumber(), "D", departTime);

                            //print current event and its departure event
                       /* System.out.println(currentEvent.getEventNumber() + "\t" + currentEvent.getEventStatus() + "\t"
                                + currentEvent.getTime());
                        System.out.println(eDepart.getEventNumber() + "\t" + eDepart.getEventStatus() + "\t"
                                + eDepart.getTime());
*/
                            eventPriorityQueue.add(eDepart);
                            FEL.add(currentEvent);
                            FEL.add(eDepart);

                            if (isChannel1Available) {
                                isChannel1Available = false;
                            } else if (isChannel2Available) {
                                isChannel2Available = false;
                            } else if (isChannel3Available) {
                                isChannel3Available = false;
                            }
                        }
                    }
                }

                // If currentEvent is departure event remove that event from the priority queue and release one channel
                else if (currentEvent.eventStatus.equals("D")) {
                    if (!isChannel1Available) {
                        isChannel1Available = true;
                    } else if (!isChannel2Available) {
                        isChannel2Available = true;
                    } else if (!isChannel3Available) {
                        isChannel3Available = true;
                    }
                }

                // Print the FEL
            /*while (!FEL.isEmpty()) {
                Event event = FEL.poll();
                System.out.println(event.getEventNumber() + "\t" + event.getEventStatus() + "\t" + event.getTime());
            }*/
            }
        }

        if (lostMessagesCount == 0) {
            return 0;
        }
        return lostMessagesCount;
    }

   
}
