package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.util.ElapsedTime;

@Autonomous // mark as autonomous so that it shows up under the auto section in the driver hub
public class REVStarterBotAutoRed extends LinearOpMode { // this is the name of the file without the extension


    //define the motors that the code is going to use
    private DcMotor flywheel;
    private DcMotor coreHex;
    private DcMotor leftFrontMotor; // mecanum drive needs 4 motors
    private CRServo servo;
    private DcMotor rightFrontMotor;
    private DcMotor leftBackMotor;
    private DcMotor rightBackMotor;


    private static final int bankVelocity = 1300; // for the flywheel speeds
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
    public void runOpMode() { // this runs when you press the INITIALIZE button on the driver hub
        awake();
        initialization();
        //update();
    }

    void awake() { // this is a function that will let the code know what all of the hardware is plugged into so it knows what to do


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
        leftFrontMotor.setDirection(DcMotor.Direction.REVERSE); // these might need to change depending on motor directions
        // currently it seems like only 1 wheel will go the right way but i can't test it because the drivetrain
        // isn't ready yet

    } // how the code knows where a function stops

    void initialization() {

        //On initilization waits for the driver to press PLAY
        while (opModeInInit()) {

            telemetry.addLine("Ready!");
            waitForStart();
            doAutoRed();


        }
    }
    private void autoDrive(double speed, int leftDistanceInch, int rightDistanceInch, int timeout_ms) { // function ptovided by rev for autodrive
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

    private void BANK_SHOT_AUTO() { //this is a function for shooting the preloaded artifact
        ((DcMotorEx) flywheel).setVelocity(bankVelocity);
        servo.setPower(-1);
        if (((DcMotorEx) flywheel).getVelocity() >= bankVelocity - 100) {
            coreHex.setPower(1);
        } else {
            coreHex.setPower(0);
        }

    }
    private void doAutoRed() { // provided by rev

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