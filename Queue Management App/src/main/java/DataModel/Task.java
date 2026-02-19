package DataModel;

public class Task {
    private int id;
    private int arrivalTime;
    private int serviceTime;
    private int startProcessingTime;

    public Task(int id, int arrivalTime, int serviceTime) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.serviceTime = serviceTime;
        this.startProcessingTime = -1;
    }

    public int getId() {
        return id;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getServiceTime() {
        return serviceTime;
    }

    public void setServiceTime(int serviceTime) {
        this.serviceTime = serviceTime;
    }

    public void setStartProcessingTime(int startProcessingTime) {
        this.startProcessingTime = startProcessingTime;
    }

    public int getStartProcessingTime() {
        return startProcessingTime;
    }

    public int getWaitingTime() {
        int waitingTime = startProcessingTime - arrivalTime;

        if (waitingTime < 0) {
            return 0;
        }
        return waitingTime;
    }

    @Override
    public String toString() {
        return "Client (id=" + id + ", arrivalTime=" + arrivalTime + ", serviceTime=" + serviceTime + ")";
    }
}

