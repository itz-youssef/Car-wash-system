Resource book : ⭐ Operating System Concepts – 9th Edition
Car Wash Concurrency Simulation

This project implements a multi-threaded car wash and gas station simulation using classic Operating System concurrency concepts. It models real-world interactions between arriving cars, a bounded waiting area, and multiple service pumps—showcasing semaphores, mutex locks, and producer–consumer synchronization.

🚦 Overview

Cars (Producers) arrive continuously and attempt to enter a fixed-size waiting queue. If the queue is full, they wait until space is available.

Pumps (Consumers) run in parallel and pick up cars as soon as they appear in the queue. If no cars are available, pumps wait.

Semaphores manage resource availability:

empty → remaining queue slots

full → number of cars waiting

mutex → thread-safe queue access

pumps → available service bays

The system ensures a fully synchronized, race-condition-free workflow.

🧠 Key Concepts Used

Multithreading

Semaphores (custom implementation)

Bounded Buffer (Producer–Consumer model)

Mutual Exclusion (Mutex)

Thread coordination & signaling

Graceful shutdown using poison pills

📌 Features

Cars log arrival and queue entry

Pumps log car assignment, service start, and completion

Randomized service times for realism

Configurable:

Queue capacity

Number of pumps

Number of cars

Clean shutdown after all cars are processed


🗂️ Project Structure


ServiceStation2 (Main)
 ├── Creates queue, semaphores, pumps, and cars
 ├── Starts producer and consumer threads
 └── Handles shutdown

BoundedQueue
 ├── Fixed-size queue
 ├── empty/full semaphores
 └── Mutex for thread safety

Car (Producer)
 └── Arrives → waits → enters queue

Pump (Consumer)
 ├── Takes car from queue
 ├── Acquires service bay
 └── Performs service + logs


▶️ How to Run

Enter:

Waiting area capacity

Number of pumps

Number of cars



📉 Example Behavior

Cars arrive and join the queue.

Pumps take cars, start service, and release bays when done.

Queue and pump activities are logged in real time.


📚 Learning Outcomes

This project demonstrates practical application of:

Thread synchronization

Shared resource management

Coordination of multiple producer and consumer threads

Avoiding race conditions using semaphores
