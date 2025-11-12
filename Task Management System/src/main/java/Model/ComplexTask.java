package Model;

import java.util.*;

public class ComplexTask extends Task {
    private List<Task> subTasks;
    private int startHour;
    private int endHour;

    public ComplexTask(int idTask, String statusTask) {
        super(idTask, statusTask);
        this.subTasks = new ArrayList<>();
    }

    public void addSubTask(SimpleTask task) {
        subTasks.add(task);
    }

    public void deleteSubTask(Task task) {
        subTasks.remove(task);
    }

    public List<Task> getSubTasks() {
        return subTasks;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public void setEndHour(int endHour) {
        this.endHour = endHour;
    }

    public int getStartHour() {
        return startHour;
    }

    public int getEndHour() {
        return endHour;
    }

    @Override
    public int estimateDuration() {
        int duration = 0;
        for (Task task : subTasks) {
            duration += task.estimateDuration();
        }
        return duration;
    }
}
