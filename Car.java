class Car extends Thread {
    final String id;
    final BoundedQueue queue;

    public Car(String id, BoundedQueue queue) {
        this.id = id;
        this.queue = queue;
    }

    public void run() {
        try {
            System.out.println(id + " arrived");
            queue.put(this);
            System.out.println(id + " entered the queue");
        } catch (Exception e) {
            System.out.println(id + " error");
        }
    }

    public String toString() { return id; }
}



