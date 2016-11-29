/**
 * Created by nimje on 11/28/2016.
 */
public class Event implements Comparable<Event>{
    int eventNumber;
    String eventStatus;
    int time;

    public Event(int eventNumber, String eventStatus, int time) {
        this.eventNumber = eventNumber;
        this.eventStatus = eventStatus;
        this.time = time;
    }

    public String getEventStatus() {
        return eventStatus;
    }

    public void setEventStatus(String eventStatus) {
        this.eventStatus = eventStatus;
    }

    public int getEventNumber() {
        return eventNumber;
    }

    public void setEventNumber(int eventNumber) {
        this.eventNumber = eventNumber;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void printEvent(){
        System.out.println(eventNumber + "\t" + eventStatus + "\t" + time);
    }

    public int compareTo(Event e){
        if (this.time < e.time)
            return -1;
        else if (this.time > e.time)
            return 1;
        return 0;
    }
}
