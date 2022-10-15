package task_5;

public class MeasureMethodTime {

    @MeasureTime
    public void measure() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
