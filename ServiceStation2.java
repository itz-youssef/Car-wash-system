import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;

class Semaphore {
    int permits = 0;

    Semaphore(int permits) {
        this.permits = permits;
    }
    public synchronized void acquire() throws InterruptedException {
        while (permits <= 0) {
            wait();
        }
        permits--;
    }

    public synchronized void release() {
        permits++;
        notifyAll();
    }
}


class BoundedQueue {
    public Queue<Object> queue;
    private int MAX_CAPACITY;
    private Semaphore empty;
    private Semaphore full;
    private Semaphore mutex;

    BoundedQueue(int capacity) {
        this.MAX_CAPACITY = capacity;
        this.queue = new LinkedList<Object>();
        this.empty = new Semaphore(MAX_CAPACITY);
        this.full = new Semaphore(0);
        this.mutex = new Semaphore(1);

    }

    public void put(Object item) throws InterruptedException {
        empty.acquire();
        mutex.acquire();
        queue.add(item);
        mutex.release();
        full.release();
    }

    public Object take() throws InterruptedException {
        full.acquire();
        mutex.acquire();
        Object item = queue.remove();
        mutex.release();
        empty.release();
        return item;
    }

}

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
}

/**
 * Pump Class - Consumer Side

 * Responsibilities:
 * - Consume cars from the bounded queue
 * - Acquire service bay using Pumps semaphore
 * - Simulate car service
 * - Log all actions (login, begin service, finish service)
 * - Handle graceful shutdown
 *
 * @author Member 3
 */

class Pump extends Thread {
    private final int pumpId;
    private final BoundedQueue queue;
    private final Semaphore pumps;
    private volatile boolean running;


    public Pump(int pumpId, BoundedQueue queue, Semaphore pumps) {
        this.pumpId = pumpId;
        this.queue = queue;
        this.pumps = pumps;
        this.running = true;
    }

    /**
     * Main thread execution - Consumer logic
     */
    @Override
    public void run() {
        while (running) {
            try {
                // Step 1: Take car from queue
                // BoundedQueue internally handles:
                // - full.acquire() (wait if queue empty)
                // - mutex.acquire() (lock queue)
                // - queue.remove() (get car)
                // - mutex.release() (unlock queue)
                // - empty.release() (signal empty slot)
                Object item = queue.take();

                // Step 2: Check for poison pill (shutdown signal)
                if (item == null) {
                    System.out.println("Pump " + pumpId + ": Received shutdown signal");
                    running = false;
                    break;
                }

                // Step 3: Cast object to Car
                Car car = (Car) item;

                // Step 4: Acquire service bay (wait if all bays occupied)
                pumps.acquire();

                // Step 5: Log car login (car picked up by pump)
                System.out.println("Pump " + pumpId + ": " + car.id + " login");

                // Step 6: Log service beginning
                System.out.println("Pump " + pumpId + ": " + car.id +
                        " begins service at Bay " + pumpId);

                // Step 7: Simulate service time (random 1-3 seconds)
                int serviceTime = 1000 + (int)(Math.random() * 2000);
                Thread.sleep(serviceTime);

                // Step 8: Log service completion
                System.out.println("Pump " + pumpId + ": " + car.id + " finishes service");

                // Step 9: Log bay release and release service bay
                System.out.println("Pump " + pumpId + ": Bay " + pumpId + " is now free");
                pumps.release();

            } catch (InterruptedException e) {
                // Handle interruption during wait or sleep
                System.out.println("Pump " + pumpId + ": Interrupted - shutting down");
                running = false;
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                // Handle any other exceptions
                System.err.println("Pump " + pumpId + ": Error - " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Final shutdown message
        System.out.println("Pump " + pumpId + ": Shutdown complete");
    }

}


class ServiceStation2 {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);

        System.out.println("Enter Waiting area capacity:");
        int QUEUE_CAPACITY = scanner.nextInt();

        System.out.println("Enter Number of service bays (pumps):");
        int NUM_OF_PUMPS = scanner.nextInt();

        System.out.println("Enter Number of cars to simulate:");
        int NUM_OF_CARS = scanner.nextInt();

        scanner.close();


        Semaphore pumpsSemaphore = new Semaphore(NUM_OF_PUMPS);
        BoundedQueue sharedQueue = new BoundedQueue(QUEUE_CAPACITY);
        Pump[] pumpThreads = new Pump[NUM_OF_PUMPS];
        Thread[] carThreads = new Thread[NUM_OF_CARS];


        System.out.println("-----------------------------------------------------");
        System.out.println("Service Station is open!");
        System.out.println("Pumps available: " + NUM_OF_PUMPS);
        System.out.println("Waiting spots in queue: " + QUEUE_CAPACITY);
        System.out.println("-----------------------------------------------------");

        for (int i = 0; i < NUM_OF_PUMPS; i++) {
            pumpThreads[i] = new Pump(i + 1, sharedQueue, pumpsSemaphore);
            pumpThreads[i].start();
        }

        System.out.println("Cars are starting to arrive...");
        for (int i = 0; i < NUM_OF_CARS; i++) {
            carThreads[i] = new Car("Car " + (i + 1), sharedQueue);
            carThreads[i].start();

            try {
                Thread.sleep((int)(Math.random() * 800) + 100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("All cars created. Waiting for them to enter queue...");
        try {
            for (Thread car : carThreads) {
                car.join();
            }
            System.out.println("-----------------------------------------------------");
            System.out.println("All " + NUM_OF_CARS + " cars are in the queue. Station entrance closed.");
            System.out.println("-----------------------------------------------------");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            for (int i = 0; i < NUM_OF_PUMPS; i++) {
                sharedQueue.put(null);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        try {
            System.out.println("Waiting for pumps to finish final services...");
            for (Pump pump : pumpThreads) {
                pump.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("-----------------------------------------------------");
        System.out.println("All services complete. Service Station is closed!");
        System.out.println("-----------------------------------------------------");
    }
}