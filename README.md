Steps used to import 2026 project into 2027 for conversion to Systemcore from roboRIO:

1. Copy 2026 project to a new folder
2. Upgrade to Gradle 9.4.1
   a. Open project in VS Code 2026
   b. Open gradle\wrapper\gradle-wrapper.properties file
   c. Change version to 9.4.1 in the distributionUrl
   d. Save changes
   e. Refresh the gradle project by closing the IDE and then re-opening
   f. Verify the version is updated by running "./gradlew --version" in Terminal window
4. Remove unused imports, save project
5. Open copied project in VS Code 2027
6. Say Yes to conversion, Click Next when prompted for build.gradle file
7. Install vendor dependencies:
   a. CTRE Phoenix (v6)
   b. PathplannerLib
   c. RevLib
   d. Commands v3 (need to uninstall v2 first)(this will break xxxx)
9. Verify Java version is 25 or higher. (type "java -version" in Terminal window
10. Add libraries for:
    a. sendableChooser replaced by Selectable from org.wpilib.tunable.util.* 
  requires >= Elastic alpha 9
    b. SmartDashboard replace with Network Tables for putData, putBoolean, putNumber, putString (create class and method to call)
12. changes:
    a. remove robotInit() from Robot.java
    b. replace "positionConversionFactor" with ??? (see SPARKMAX ChangeLog on https://github.com/wpilibsuite/SystemcoreTesting/blob/main/REV.md)
    c. ??silenceJoystickConnectionWarning replaced by ????
    d. XboxController replace by org.wpilib.driverstation.Gamepad
    e. References to CAN need CAN Port and Device ID (SparkMax, Talon, etc)
    f. SparkMax need to be configured for a motor type of VOLTAGE, and use the .SetVoltage instead of .Set for speed (What voltage should be used?  5V? 12V?)
    g. Relative encoders used with SparkMax need to add .Get() to .GetPosition()
      For example, climberEncoder.getPosition().get()
    h. Lots of changes to DriveSubsystem for swerve.  E.g. SwerveModuleState renamed to SwerveModuleVelocity, .getState() gone.
    i. Subsystems no longer extend SubsystemBase, now extend Mechanism (import org.wpilib.command3.Mechanism;)

