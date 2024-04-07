Iteration 5
SYSC3303A L2G6
Winter 2024

Masrur: README
Eric: GUI floor lamps, and worked on time delays for Floor
Adham: Implementing capacity limits and calculating number of requests received by elevator by scheduler
Garrison: Created custom cells and print statements for elevators in the GUI, and helped with the GUI in general
Marwan: Interfacing GUI with RMI and creating message log, and helped create the GUI

What is this?:
This is a submission for Iteration 5 of the SYSC3303 Group Project

This project simulates an Elevator system where there is a Floor Subsystem which controls floor events where someone is
calling an elevator, the Scheduler subsystem which reads the inputs from floors and coordinates with the elevator,
and the Elevator Subsystem which takes the processed requests from the scheduler and acts upon them.

In this iteration, a graphic user interface was implemented, simulating a control panel. The Floor class was updated to
reflect more realistic delays, Scheduler is updated to implement and observe a maximum capacity for passengers aboard an
elevator, and to redistribute floor requests between elevators when one reaches its capacity.

Installation:
Extract L2G6_milestone_5.zip
Open IntelliJ
Click Open and select L2G6_milestone_5 project folder inside the extracted folder

To run this, you must run each subsystem in a specific order by pressing the green run button next to each of their main
methods.
The order is: SchedulerReciever, ElevatorInspector, Elevator, Scheduler, and Floor.
Any other order will cause errors, as SchedulerStore is set up in Receiver, and for floor requests to do anything, elevators must be accounted for

Testing Instructions
Steps:
  Add JUnit5.8.1 to classpath for the project if not already added
  Click on the project folder in the explorer on the left, and click Ctrl+Shift+F10
  All 11 JUnit tests should run

All system files (21)
    CustomCellRenderer - Changes the colours of the error indicators in the GUI based on what type of error is handled
    Elevator.java - Class file holding the Elevator Subsystem of the simulation
    ELEVATOR_BUTTON.java - File holding an ENUM representing various button states for elevator
    ElevatorEvent.java - Class used to encapsulate the information required by floor events
    ElevatorInspector.java - File with classes used to implement GUI
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