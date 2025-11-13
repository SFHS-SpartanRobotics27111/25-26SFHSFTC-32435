/* 1. Select the mode you wish to drive with: AUTO RED or AUTO BLUE, or TELEOP
*       a. TELEOP:
*           Uses SPLIT STICK ARCADE for movement (one stick controls forward and back, other controls turning)
*
*           Pressing CROSS spins intake forward
*           Pressing TRIANGLE spins intake back
*
*           Pressing LEFT D-PAD and RIGHT D-PAD spins the agitator in different directions
*
*           Pressing OPTIONS puts flywheel in reverse
*
*           Pressing LEFT BUMPER does a far shot
*           Pressing SQUARE changes flywheel speed to far shot speed or "max" velocity
*
*           Pressing RIGHT BUMBER does a near shot
*           Pressing CIRCLE changes flywheel speed to near shot speed or "bank" velocity
*
*           Pressing nothing stops the flywheel and hex motor, if you are not manually controlling the agitator, agitator will not stutter
*
*       b. AUTO RED and AUTO BLUE:
*           For 10 seconds shoots any balls pre-loaded into RED or BLUE goal
*           Backs up a little
*           Turns around
*           Gets off of line
*/

package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@TeleOp
public class REVStarterBotTeleOpAutoJava extends LinearOpMode {

    private DcMotor flywheel;
    private DcMotor coreHex;
    private DcMotor leftFrontMotor;
    private CRServo servo;
    private DcMotor rightFrontMotor;
    private DcMotor leftBackMotor;
    private DcMotor rightBackMotor;


    private static final int bankVelocity = 1300;
    private static final int farVelocity = 1900;
    private static final int maxVelocity = 2200;
    private static final String TELEOP = "TELEOP";
    private static final String AUTO_BLUE = "AUTO BLUE";
    private static final String AUTO_RED = " AUTO RED";
    private String operationSelected = TELEOP;
    private double WHEELS_INCHES_TO_TICKS = (28 * 5 * 3) / (3 * Math.PI);
    private ElapsedTime autoLaunchTimer = new ElapsedTime();
    private ElapsedTime autoDriveTimer = new ElapsedTime();

    @Override
    public void runOpMode() {
        awake();
        initialization();
        update();
    }

    // Function for initializing variables and configuring motor directions
    void awake() {

        // Getting components of robot into variables
        flywheel = hardwareMap.get(DcMotor.class, "flywheel");
        coreHex = hardwareMap.get(DcMotor.class, "coreHex");
        leftFrontMotor = hardwareMap.get(DcMotor.class, "leftFrontMotor");
        servo = hardwareMap.get(CRServo.class, "servo");
        rightFrontMotor = hardwareMap.get(DcMotor.class, "rightFrontMotor");
        leftBackMotor = hardwareMap.get(DcMotor.class, "leftBackMotor");
        rightBackMotor = hardwareMap.get(DcMotor.class, "rightBackMotor");
        // Establishing the direction and mode for the motors
        flywheel.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        flywheel.setDirection(DcMotor.Direction.REVERSE);
        coreHex.setDirection(DcMotor.Direction.REVERSE);
        leftFrontMotor.setDirection(DcMotor.Direction.REVERSE);


        // Ensures the servo is active and ready
        servo.setPower(0);
    }

    // Function for everything that happens before start button is actually clicked (things like selecting auto or teleop process)
    void initialization() {

        //On initilization the Driver Station will prompt for which OpMode should be run - Auto Blue, Auto Red, or TeleOp
        while (opModeInInit()) {
            operationSelected = selectOperation(operationSelected, gamepad1.psWasPressed());
            telemetry.update();
        }

        waitForStart();
    }

    // Continuous function / where each main loop will happen
    void update() {

        selectMode();
    }

    private void selectMode() {

        // CANNOT USE PLAYSTATION CONTROLLER TO SWITCH BETWEEN MODES BEFORE AUTO

        // Configures robot operation depending on if you are starting on red or blue or if mode is teleop
        if (operationSelected.equals(AUTO_BLUE)) {
            doAutoBlue();
        } else if (operationSelected.equals(AUTO_RED)) {
            doAutoRed();
        } else {
            doTeleOp();
        }
    }

    /**
     * If the PS/Home button is pressed, the robot will cycle through the OpMode options following the if/else statement here.
     * The telemetry readout to the Driver Station App will update to reflect which is currently selected for when "play" is pressed.
     */
    private String selectOperation(String state, boolean cycleNext) {

        // Configures operation mode selection (already explained up above)
        if (cycleNext) {
            if (state.equals(TELEOP)) {
                state = AUTO_BLUE;
            } else if (state.equals(AUTO_BLUE)) {
                state = AUTO_RED;
            } else if (state.equals(AUTO_RED)) {
                state = TELEOP;
            } else {
                telemetry.addData("WARNING", "Unknown Operation State Reached - Restart Program");
            }
        }
        telemetry.addLine("Press Home Button to cycle options");
        telemetry.addData("CURRENT SELECTION", state);
        if (state.equals(AUTO_BLUE) || state.equals(AUTO_RED)) {
            telemetry.addLine("Please remember to enable the AUTO timer!");
        }
        telemetry.addLine("Press START to start your program");
        return state;
    }

    //TeleOp Code

    /**
     * If TeleOp was selected or defaulted to, the following will be active upon pressing "play".
     */
    private void doTeleOp() {

        if (opModeIsActive()) {
            while (opModeIsActive()) {
                // Calling our methods while the OpMode is running
                splitStickArcadeDrive();
                setFlywheelVelocity();
                manualCoreHexAndServoControl();

                telemetry.addData("Flywheel Velocity", ((DcMotorEx) flywheel).getVelocity());
                telemetry.addData("Flywheel Power", flywheel.getPower());
                telemetry.update();
            }
        }
    }

    /**
     * Controls for the drivetrain. The robot uses a split stick style arcade drive.
     * Forward and back is on the left stick. Turning is on the right stick.
     */
    private void splitStickArcadeDrive() {

        float X;
        float Y;

       double x = gamepad1.right_stick_x;
       double y = gamepad1.left_stick_y;
       double rotation = gamepad1.right_stick_x;

        leftFrontMotor.setPower(y + x + rotation);
        rightFrontMotor.setPower(y + x + rotation);
        leftBackMotor.setPower(y + x + rotation);
        rightBackMotor.setPower(y + x + rotation);
    }

    /**
     * Manual control for the Core Hex powered feeder and the agitator servo in the hopper
     */
    private void manualCoreHexAndServoControl() {

        // Manual control for the Core Hex intake
        if (gamepad1.cross) {
            coreHex.setPower(0.5);
        } else if (gamepad1.triangle) {
            coreHex.setPower(-0.5);
        }

        // Manual control for the hopper's servo
        if (gamepad1.dpad_left) {
            servo.setPower(1);
        } else if (gamepad1.dpad_right) {
            servo.setPower(-1);
        }
    }

    /**
     * This if/else statement contains the controls for the flywheel, both manual and auto.
     * Circle and Square will spin up ONLY the flywheel to the target velocity set.
     * The bumpers will activate the flywheel, Core Hex feeder, and servo to cycle a series of balls.
     */
    private void setFlywheelVelocity() {//

    if (gamepad1.options) {
            flywheel.setPower(0.5);
        } else if (gamepad1.left_bumper) {
            FAR_POWER_AUTO();
        } else if (gamepad1.right_bumper) {
            BANK_SHOT_AUTO();
        } else if (gamepad1.circle) {
            ((DcMotorEx) flywheel).setVelocity(bankVelocity);
        } else if (gamepad1.square) {
            ((DcMotorEx) flywheel).setVelocity(maxVelocity);
        } else {
            ((DcMotorEx) flywheel).setVelocity(0);
            coreHex.setPower(0);
            // The check below is in place to prevent stuttering with the servo. It checks if the servo is under manual control!
            if (!gamepad1.dpad_right && !gamepad1.dpad_left) {
                servo.setPower(0);
            }
        }
    }

//Automatic Flywheel controls used in Auto and TeleOp

    /**
     * The bank shot or near velocity is intended for launching balls touching or a few inches from the goal.
     * When running this function, the flywheel will spin up and the Core Hex will wait before balls can be fed.
     * The servo will spin until the bumper is released.
     */
    private void BANK_SHOT_AUTO() {
        ((DcMotorEx) flywheel).setVelocity(bankVelocity);
        servo.setPower(-1);
        if (((DcMotorEx) flywheel).getVelocity() >= bankVelocity - 100) {
            coreHex.setPower(1);
        } else {
            coreHex.setPower(0);
        }
    }

    /**
     * The far power velocity is intended for launching balls a few feet from the goal. It may require adjusting the deflector.
     * When running this function, the flywheel will spin up and the Core Hex will wait before balls can be fed.
     * The servo will spin until the bumper is released.
     */
    private void FAR_POWER_AUTO() {
        ((DcMotorEx) flywheel).setVelocity(farVelocity);
        servo.setPower(-1);
        if (((DcMotorEx) flywheel).getVelocity() >= farVelocity - 100) {
            coreHex.setPower(1);
        } else {
            coreHex.setPower(0);
        }
    }

//Autonomous Code
//For autonomous, the robot will launch the pre-loaded 3 balls then back away from the goal, turn, and back up off the launch line.

    /**
     * For autonomous, the robot is using a timer and encoders on the drivetrain to move away from the target.
     * This method contains the math to be used with the inputted distance for the encoders, resets the elapsed timer, and
     * provides a check for it to run so long as the motors are busy and the timer has not run out.
     */
    private void autoDrive(double speed, int leftDistanceInch, int rightDistanceInch, int timeout_ms) {
        autoDriveTimer.reset();
        leftFrontMotor.setTargetPosition((int) (leftFrontMotor.getCurrentPosition() + leftDistanceInch * WHEELS_INCHES_TO_TICKS));
        rightBackMotor.setTargetPosition((int) (rightFrontMotor.getCurrentPosition() + rightDistanceInch * WHEELS_INCHES_TO_TICKS));
        leftBackMotor.setTargetPosition((int) (leftFrontMotor.getCurrentPosition() + leftDistanceInch * WHEELS_INCHES_TO_TICKS));
        rightFrontMotor.setTargetPosition((int) (rightFrontMotor.getCurrentPosition() + rightDistanceInch * WHEELS_INCHES_TO_TICKS));
        leftFrontMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightFrontMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBackMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBackMotor.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBackMotor.setPower(Math.abs(speed));
        rightBackMotor.setPower(Math.abs(speed));
        leftFrontMotor.setPower(Math.abs(speed));
        rightFrontMotor.setPower(Math.abs(speed));
        while (opModeIsActive() && (leftFrontMotor.isBusy() || rightFrontMotor.isBusy()) && autoDriveTimer.milliseconds() < timeout_ms) {
            idle();
        }
        leftFrontMotor.setPower(0);
        rightFrontMotor.setPower(0);
        leftFrontMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        rightFrontMotor.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
    }

    /**
     * Blue Alliance Autonomous
     * The robot will fire the pre-loaded balls until the 10 second timer ends.
     * Then it will back away from the goal and off the launch line.
     */
    private void doAutoBlue() {

        if (opModeIsActive()) {

            telemetry.addData("RUNNING OPMODE", operationSelected);
            telemetry.update();

            autoLaunchTimer.reset();
            while (opModeIsActive() && autoLaunchTimer.milliseconds() < 10000) {

                BANK_SHOT_AUTO();

                telemetry.addData("Launcher Countdown", autoLaunchTimer.seconds());
                telemetry.update();
            }

            ((DcMotorEx) flywheel).setVelocity(0);
            coreHex.setPower(0);
            servo.setPower(0);
            // Back Up
            autoDrive(0.5, -12, -12, 5000);
            // Turn
            autoDrive(0.5, -8, 8, 5000);
            // Drive off Line
            autoDrive(1, -50, -50, 5000);
        }
    }

    /**
     * Red Alliance Autonomous
     * The robot will fire the pre-loaded balls until the 10 second timer ends.
     * Then it will back away from the goal and off the launch line.
     */
    private void doAutoRed() {

        if (opModeIsActive()) {

            telemetry.addData("RUNNING OPMODE", operationSelected);
            telemetry.update();

            autoLaunchTimer.reset();
            while (opModeIsActive() && autoLaunchTimer.milliseconds() < 10000) {

                BANK_SHOT_AUTO();

                telemetry.addData("Launcher Countdown", autoLaunchTimer.seconds());
                telemetry.update();
            }

            ((DcMotorEx) flywheel).setVelocity(0);
            coreHex.setPower(0);
            servo.setPower(0);
            // Back Up
            autoDrive(0.5, -12, -12, 5000);
            // Turn
            autoDrive(0.5, 8, -8, 5000);
            // Drive off Line
            autoDrive(1, -50, -50, 5000);
        }
    }
}
//