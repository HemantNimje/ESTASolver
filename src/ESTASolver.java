import cc.mallet.util.*;
import cc.mallet.util.Univariate;
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
    private static long lostMessagesCount = 0;
    private static long avgLostMessagesCount = 0;
    private static int x = 0;
    private static long sum = 0;
    private static long sumsq = 0;
    private static long totalMessagesSent = 0;
    private static double msgLostProbability;
    private static double[] lostMessages = new double[10000];
    private static double[] sentMessages = new double[10000];
    private static double coVariance = 0;
    private static double variance = 0;
    private static double avgLostMessages = 0;
    private static double lambda = 0;
    private static double sigmasq = 0;
    private static double se = 0;
    private static double re = 0;
    private static double avgSentMessages;
    private static double constantC;
    private static double z = 0;
    private static double zSum = 0;

    public static void main(String args[]) throws IOException {
        ESTASolver solver = new ESTASolver();

        /*lostMessagesCount = solver.getLostMessagesCount();
        // Print the number of messages that are lost
        System.out.println("Number of msg lost: " + lostMessagesCount);
*/
        //avgLostMessagesCount = solver.getAvgLostMessagesCount();
        // Print the average number of messages that are lost
        //System.out.println("Average number of lost messages: " + avgLostMessagesCount);


        for (int i = 0; i < 10000; i++) {
            lostMessagesCount = solver.getLostMessagesCount();
        }
        avgLostMessagesCount = lostMessagesCount / 10000;

        System.out.println("Average number of lost messages: " + avgLostMessagesCount);

        System.out.println("Total lost messages: " + lostMessagesCount);
        System.out.println("Total sent messages: " + totalMessagesSent);

        System.out.println("Probability of a message being lost: " + ((double) lostMessagesCount / totalMessagesSent));


        lostMessagesCount = 0;
        totalMessagesSent = 0;

        //EXERCISE 3
        for (int i = 0; i < 1000; i++) {
            lostMessagesCount = solver.getLostMessagesCount();
            lostMessages[i] = lostMessagesCount;
            sentMessages[i] = totalMessagesSent;
        }

        System.out.println("\nTotal lost messages: " + lostMessagesCount);
        System.out.println("Total sent messages: " + totalMessagesSent);

        Univariate uLostMessages = new Univariate(lostMessages);
        Univariate uSentMessages = new Univariate(sentMessages);

        coVariance = StatFunctions.cov(uLostMessages, uSentMessages);

        variance = uSentMessages.variance();

        //Mue - average number of messages sent
        avgSentMessages = totalMessagesSent / 1000;

        constantC = coVariance / variance;

        System.out.println("Covariance: " + coVariance);
        System.out.println("Variance: " + variance);
        System.out.println("Mue: " + avgSentMessages);
        System.out.println("Constant C: " + constantC);

        lostMessagesCount = 0;
        totalMessagesSent = 0;


        // IMC implementation using the R script provided by prof. Todd
        for(int i = 0; i< 10000; i++){
            lostMessagesCount = solver.getLostMessagesCount();
            sum = lostMessagesCount;
            sumsq = lostMessagesCount * lostMessagesCount;
            z = lostMessagesCount + constantC * (totalMessagesSent - avgSentMessages);
            zSum = zSum + z;
        }

        lambda = (sum / 10000);
        sigmasq = (sumsq + (lambda * lambda * 10000))/(10000-1);
        se = Math.sqrt(sigmasq/10000);
        re = se / lambda;

        msgLostProbability = (double) sum / totalMessagesSent;

        // Average number of lost messages
        avgLostMessages = lostMessagesCount / 10000;

        System.out.println("\nAverage number of lost messages: "+avgLostMessages);
        System.out.println("Total lost messages: "+ lostMessagesCount);
        System.out.println("Total sent messsages: "+ totalMessagesSent);
        System.out.println("Probability of messages being lost: "+ msgLostProbability);
        System.out.println("\nSample mean: "+lambda);
        System.out.println("Sample variance: "+sigmasq);
        System.out.println("Standard error: "+se);
        System.out.println("Relative error: "+re);
        System.out.println("New value of Z: "+zSum);

    }

    private long getLostMessagesCount() {

        // Titles for the FEL
        /*System.out.println("EventName" + "\t" + "EventStatus" + "\t" + "EventTime");*/

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

        totalMessagesSent += eventPriorityQueue.size();

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
