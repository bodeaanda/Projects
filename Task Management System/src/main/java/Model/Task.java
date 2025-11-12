package Model;

import java.io.*;

public abstract class Task implements Serializable {
    protected int idTask;
    protected String statusTask;

    public Task(int idTask, String statusTask) {
        this.idTask = idTask;
        this.statusTask = statusTask;
    }
    public void setStatusTask(String statusTask) {
        this.statusTask = statusTask;
    }
    public String getStatusTask() {
        return statusTask;
    }

    public int getIdTask() {
        return idTask;
    }

    public abstract int estimateDuration();

    public void saveToFile(String fileName) {
        try(ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(this);
            System.out.println("Saved to " + fileName);
        } catch (IOException e) {
            System.out.println("Error saving to " + fileName);
        }
    }
    public static TaskManagement loadFromFile(String fileName) {
        try(ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (TaskManagement) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading from " + fileName);
            return null;
        }
    }
}
