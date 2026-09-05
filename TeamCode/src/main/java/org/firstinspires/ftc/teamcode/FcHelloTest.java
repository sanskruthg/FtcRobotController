package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.util.ElapsedTime;
@Autonomous
public class FcHelloTest extends OpMode {

    private final ElapsedTime runtime = new ElapsedTime();

    @Override
    public void init() {
        telemetry.addData("Fc Hello", "Robo Mavericks");
    }

    @Override
    public void loop() {
        telemetry.addData("Status", "Current Time: " + runtime);
    }
}
