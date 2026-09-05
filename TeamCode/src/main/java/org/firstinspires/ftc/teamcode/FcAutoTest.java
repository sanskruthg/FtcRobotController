package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
import org.firstinspires.ftc.teamcode.pedroPathing.Constants;
import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

/**
 * FC's auto test pedro pathing program that moves robot does
 * <p>
 * 1) Move from startPos to pose1
 * 2) Pause at pose1 for 5 seconds
 * 3) Move from pose2 to pose3
 * 4) Done.
 */
@Autonomous(name = "FcAutoTest (OpMode)", group = "Autonomous")
public class FcAutoTest extends OpMode {

    private Follower follower;
    private ElapsedTime timer;

    // Define Poses directly (X, Y, Heading in Radians)
    private final Pose startPose = new Pose(0, 0, Math.toRadians(0));
    private final Pose pose1     = new Pose(24, 0, Math.toRadians(0));
    private final Pose pose2     = new Pose(24, 24, Math.toRadians(90));

    private PathChain pathChain1;
    private PathChain pathChain2;

    // Simplified 3-State Machine
    private enum State {
        PATH_1,
        PAUSE,
        PATH_2,
        DONE
    }

    private State currentState = State.PATH_1;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(startPose);
        timer = new ElapsedTime();

        // Build PathChain 1 using BezierLine between startPose and pose1
        pathChain1 = follower.pathBuilder()
                .addPath(new BezierLine(startPose, pose1))
                .setLinearHeadingInterpolation(startPose.getHeading(), pose1.getHeading())
                .build();

        // Build PathChain 2 using BezierLine between pose1 and pose2
        pathChain2 = follower.pathBuilder()
                .addPath(new BezierLine(pose1, pose2))
                .setLinearHeadingInterpolation(pose1.getHeading(), pose2.getHeading())
                .build();

        telemetry.addData("Status", "Initialized");
    }

    @Override
    public void start() {
        // Begin PathChain 1 on play that moves from startPose to pose1.
        follower.followPath(pathChain1);
        currentState = State.PATH_1;
    }

    @Override
    public void loop() {
        // Must be called continuously every loop cycle
        follower.update();

        // When follower is still busy means the robot is still moving.
        switch (currentState) {
            case PATH_1:
                if (!follower.isBusy()) {
                    // PATH_1 has finished (i.e. not busy), reset
                    // timer and enter pause state.
                    timer.reset();
                    currentState = State.PAUSE;
                }
                break;

            case PAUSE:
                // Wait for 5s in pause state.
                if (timer.seconds() >= 5.0) {
                    // Start the PathChain2 by moving from pose1 to pose2.
                    follower.followPath(pathChain2);
                    // Set PATH_2 state.
                    currentState = State.PATH_2;
                }
                break;

            case PATH_2:
                if (!follower.isBusy()) {
                    // PATH_2 has finished (i.e. follower not busy.)
                    // Set state to DONE.
                    currentState = State.DONE;
                }
                break;

            case DONE:
                // Done!!! print something to telemetry (or console).
                telemetry.addData("Status", "Auto Finished!");
                break;
        }

        telemetry.addData("State", currentState);
        telemetry.addData("X", follower.getPose().getX());
        telemetry.addData("Y", follower.getPose().getY());
        telemetry.addData("Heading (deg)", Math.toDegrees(follower.getPose().getHeading()));
    }
}
