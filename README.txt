Iteration 4
SYSC3303A L2G6
Winter 2024

Masrur: Updating floor and ElevatorEvent to include Error column in input file
Eric: README and UML Diagrams
Adham: Timing Diagrams
Garrison: Scheduler Receiver add new case
Marwan: Simulate faults in Elevator

What is this?:
This is a submission for Iteration 4 of the SYSC3303 Group Project

This project simulates an Elevator system where there is a Floor Subsystem which controls floor events where someone is calling an elevator, the Scheduler
Subsystem which reads the inputs from floors and coordinates with the elevator, and the Elevator Subsystem which takes the processed requests from the scheduler
and acts upon them.

In this iteration, the codebase was organized, removing unused classes, test.txt was updated with an extra column to indicate whether there was a fault 
and what type, Floor class was updated to read these faults and send the appropriate message to signal that a fault occurs, Scheduler is updated to 
detect incoming faults and transmit fault information to elevators, Elevator is updated to implement said faults, and if the fault disables the 
elevator, the Scheduler is now able to remove out of order elevators from being scheduled and reassign their unserviced requests.

Installation:
Extract L2G6_milestone_4.zip
Open IntelliJ
Click Open and select L2G6_milestone_4 project folder inside the extracted folder

To run this, you must run each subsystem in a specific order by pressing the green run button next to each of their main methods.
The order is: SchedulerReciever, Elevator, Scheduler and Floor.
Any other order will cause errors, as schedulerstore is set up in receiver, and for floor requests to do anything, elevators must be accounted for

Testing Instructions
Steps:
  Add JUnit5.8.1 to classpath for the project if not already added
  Click on the project folder in the explorer on the left, and click Ctrl+Shift+F10
  All 11 JUnit tests should run

All system files (19)
    Elevator.java - Class file holding the Elevator Subsystem of the simulation
    ELEVATOR_BUTTON.java - File holding an ENUM representing various button states for elevator
    ElevatorEvent.java - Class used to encapsulate the information required by floor events
    ElevatorState.java - Interface for Elevator States
    Floor.java - Class file holding the Floor Subsystem of the simulation
    HelperFunctions.java - Helper functions, mainly for string and byte[] messages
    Idle.java - Class for the Idle state for Elevator
    LoadingUnloading.java - Class for the Loading/Unloading state for Elevator
    Moving.java - Class for the Moving state for Elevator
    Scheduler.java - Class file holding the Logic of the Scheduler Subsystem
    SchedulerReceiver.java - Class file holding the communcation logic for Scheduler Subsystem
    SchedulerStore.java - Shared data file for the Scheduler parts, to pass information between them
    SchedulerStoreInt.java - Interface for shared data file

    test.txt - Test input for floor subsystem

    ElevatorEventTest.java - Test file for the ElevatorEvent class
    ElevatorTest.java - Test file for Elevator Subsystem
    FloorTest.java - Test file for Floor Subsystem
    SchedulerReceiverTest.java - Test file for Scheduler Receiver
    SchedulerTest.java - Test file for Scheduler Subsystem

    README.txt

All diagrams can be found in the Diagrams folder of the IntelliJ project or the github