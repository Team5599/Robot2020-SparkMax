/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.ColorSensorV3;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.SpeedControllerGroup;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to each mode, as described in the TimedRobot
 * documentation. If you change the name of this class or the package after
 * creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>();

  CANSparkMax m_leftOne, m_leftTwo, m_rightOne, m_rightTwo;

  SpeedControllerGroup leftDrive, rightDrive;

  DifferentialDrive driveTrain;

  XBoxController controller;

  I2C.Port port;
  ColorSensorV3 colorSensor;

  NetworkTable table;
  NetworkTableEntry tx, ty, ta;
  double xOffset, yOffset, targetArea;

  String gameData;

  /**
   * This function is run when the robot is first started up and should be used
   * for any initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    m_leftOne = new CANSparkMax(0, MotorType.kBrushless);
    m_leftTwo = new CANSparkMax(1, MotorType.kBrushless);

    m_rightOne = new CANSparkMax(2, MotorType.kBrushless);
    m_rightTwo = new CANSparkMax(3, MotorType.kBrushless);

    leftDrive = new SpeedControllerGroup(m_leftOne, m_leftTwo);
    rightDrive = new SpeedControllerGroup(m_rightOne, m_rightTwo);

    driveTrain = new DifferentialDrive(leftDrive, rightDrive);

    controller = new XBoxController(0);

    port = I2C.Port.kOnboard;
    colorSensor = new ColorSensorV3(port);

    table = NetworkTableInstance.getDefault().getTable("limelight");
    tx = table.getEntry("tx");
    ty = table.getEntry("ty");
    ta = table.getEntry("ta");
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like diagnostics that you want ran during disabled, autonomous,
   * teleoperated and test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
    xOffset = tx.getDouble(0.0);
    yOffset = ty.getDouble(0.0);
    targetArea = ta.getDouble(0.0);

    gameData = DriverStation.getInstance().getGameSpecificMessage();

    SmartDashboard.putNumber("Proximity", colorSensor.getProximity());
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different autonomous modes using the dashboard. The sendable chooser
   * code works with the Java SmartDashboard. If you prefer the LabVIEW Dashboard,
   * remove all of the chooser code and uncomment the getString line to get the
   * auto name from the text box below the Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure below with additional strings. If using the SendableChooser
   * make sure to add them to the chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);
  }

  /**
   * This function is called periodically during autonomous.
   */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
    case kCustomAuto:
      // Put custom auto code here
      break;
    case kDefaultAuto:
    default:
      driveTrain.tankDrive(0.45, 0.45);

      if (colorSensor.getProximity() > 140) {
        driveTrain.tankDrive(-1, -1);
        Timer.delay(0.5);
        driveTrain.tankDrive(-1, 1);
        Timer.delay(1);
      }
      break;
    }
  }

  /**
   * This function is called periodically during operator control.
   */
  @Override
  public void teleopPeriodic() {
    if (controller.getXButton()) {
      if (xOffset < -1) {
        driveTrain.tankDrive(-0.5, 0.5);
      } else if (xOffset > 9) {
        driveTrain.tankDrive(0.5, -0.5);
      }
    } else {
      driveTrain.tankDrive(-(controller.getLeftThumbstickY()), -(controller.getRightThumbstickY()));
    }
  }

  /**
   * This function is called periodically during test mode.
   */
  @Override
  public void testPeriodic() {
  }
}
