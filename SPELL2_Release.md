The release 2.0.5 introduces several improvements and new features.

## Performance ##

Execution speed is a strong need in some situations in Spacecraft control automation. The performance of the execution environment has been improved in this release.

## Error recovery capabilities ##

In previous versions, and in the case of having Python exceptions during the execution, the procedure had to be restarted. In such a situation, all the operations already performed by the procedure would need to be repeated again.

With the error recovery feature, the user can recover from the failure and continue the execution from the same point. The current execution position and all variable values are restored after the recovery.

## New GUI features ##

  * The **Outline view** gives a good overview of the procedure structural elements. From this view, the user can see at a glance all Step and Goto instructions, plus the functions declared in the procedure. From this view it is easy to navigate through the procedure source code.

  * The **Watch of variables** view is useful for procedure debugging during development or in case of contingency. It allows inspecting variable values, modifying them, or monitor them for changes.

  * The **Call Stack view** gives information about the execution history of the procedure. It gives information about which functions have been executed and when. By using this view and while in the Code Presentation, the user can see what happened in past executions (i.e. notification data and messages)

  * **Breakpoints** have been added, so that the execution can be stopped automatically. Part of this feature is the "Run to this line" action, where a temporary breakpoint is used to run the procedure until the point specified by the user, then pause.

  * Goto has been improved with the "Goto this line" feature in the Code Presentation pop-up menu.

  * The GUI internal models have been improved in order to reduce complexity and increase the execution speed.

## Enhanced Development Environment ##

The SPELL DEV application, used for developing procedures, has been also improved.

  * Each project may have an associated TM/TC database (Ground Control System database) assigned

  * The database items (telemetry points and telecommands) are made available to the user during procedure development. This enables a number of new features like the automatically generated code snippets.

  * The user can define and use custom code templates

  * The creation of procedures is better controlled, via the new procedure creation dialog.

  * A **Semantic Checker** has been added to the application. This mechanism checks a fairly big set of rules and coding guidelines, ensuring the correctness of the procedure source code. The syntax checking just ensures a correct Python syntax. In addition, the semantic check ensures that (1) the telemetry point and telecommand names being used do exist in the TM/TC database, (2) the modifiers and arguments used in the SPELL functions are consistent and complete, (3) the code does not contain discouraged functions or constructs (i.e. ensures the compliance with the coding guidelines), (4) defined Steps and Gotos are consistent... and much more. We are currently working on many  enhancements for this component.
