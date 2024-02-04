# SYSC3303-Project

Iteration 1
SYSC3303A L2G6
Winter 2024

Masrur - Diagrams
Garrison - README, Testing
Eric - Floor Subsystem
Marwan - Scheduler/EventQueue
Adham - Elevator Subsystem

For test cases we used Junit5
Steps:
  Add JUnit5.8.1 to classpath for the project if not already added
  Click on the project folder in the explorer on the left, and click Ctrl+Shift+F10
  All 11 JUnit tests should run

All files (13)
    Elevator.java - Class file holding the Elevator Subsystem of the simulation
    ElevatorTest.java - Test file for Elevator Subsystem
    ELEVATOR_BUTTON.java - File holding an ENUM representing various button states for elevator
    ElevatorEvent.java - Class used to encapsulate the information required by elevator and floor events
    ElevatorEventTest.java - Test file for the ElevatorEvent class
    EventQueue.java - Thread-Safe class to synchronize communication between all subsystems of this simulation
    EventQueueTest.java - Test file for the EventQueue
    Floor.java - Class file holding the Floor Subsystem of the simulation
    FloorTest.java - Test file for Floor Subsystem
    Main.java - Main class, runs the simulation
    Scheduler.java - Class file holding the Scheduler Subsystem of the simulation
    SchedulerTest.java - Test file for Scheduler Subsystem
    test.txt - Test input for floor subsystem
    README.txt****
