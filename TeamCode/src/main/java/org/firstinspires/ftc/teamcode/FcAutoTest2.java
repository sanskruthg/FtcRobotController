package org.firstinspires.ftc.teamcode;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import com.pedropathing.paths.PathChain;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.ElapsedTime;

/**
 * FcAutoTest2, same as FcAutoTest, but this one uses LinearOpMode,
 * and we set up and control the loop and do the iteration ourselves.
 */
@Autonomous(name = "FcAutoTest2 (LinearOpMode)", group = "Autonomous")
public class FcAutoTest2 extends LinearOpMode {
    private Follower follower;
    private ElapsedTime waitTimer;

    // Define states for our autonomous state machine
    private enum State {
        FOLLOW_PATH_1,
        WAIT_STATE,
        FOLLOW_PATH_2,
        IDLE
    }

    private State currentState = State.FOLLOW_PATH_1;

    // Define field poses (X, Y, Heading in Radians)
    private final Pose startPose = new Pose(0, 0, Math.toRadians(0));
    private final Pose midPose   = new Pose(24, 0, Math.toRadians(0));
    private final Pose endPose   = new Pose(24, 24, Math.toRadians(90));

    private PathChain path1;
    private PathChain path2;

    @Override
    public void runOpMode() {
        // Initialize PedroPathing follower and set initial pose
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startPose);

        // Build Path 1: Drive forward 24 inches
        path1 = follower.pathBuilder()
                .addPath(new BezierLine(startPose, midPose))
                .setLinearHeadingInterpolation(startPose.getHeading(), midPose.getHeading())
                .build();

        // Build Path 2: Drive left 24 inches and turn 90 degrees
        path2 = follower.pathBuilder()
                .addPath(new BezierLine(midPose, endPose))
                .setLinearHeadingInterpolation(midPose.getHeading(), endPose.getHeading())
                .build();

        waitTimer = new ElapsedTime();

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        // Wait for Play/Start button to be clicked.
        waitForStart();

        // Start following the first path immediately upon start
        follower.followPath(path1);

        // While op mode is active and the Stop button on the driver station
        // has not yet been pressed.
        while (opModeIsActive() && !isStopRequested()) {
            // Update localization and drive motor outputs
            // Important: MUST CALL follower.update in every iteration.
            follower.update();

            switch (currentState) {
                case FOLLOW_PATH_1:
                    // Advance to wait timer when path 1 completes
                    if (!follower.isBusy()) {
                        waitTimer.reset();
                        currentState = State.WAIT_STATE;
                    }
                    break;

                case WAIT_STATE:
                    // Wait 3 seconds before starting second path
                    if (waitTimer.seconds() >= 3.0) {
                        follower.followPath(path2);
                        currentState = State.FOLLOW_PATH_2;
                    }
                    break;

                case FOLLOW_PATH_2:
                    // Finish state machine when path 2 completes
                    if (!follower.isBusy()) {
                        currentState = State.IDLE;
                    }
                    break;

                case IDLE:
                    // Motion complete
                    break;
            }

            // Diagnostic telemetry
            telemetry.addData("State", currentState);
            telemetry.addData("X", follower.getPose().getX());
            telemetry.addData("Y", follower.getPose().getY());
            telemetry.addData("Heading (deg)", Math.toDegrees(follower.getPose().getHeading()));
            telemetry.update();
        }
    }
}
