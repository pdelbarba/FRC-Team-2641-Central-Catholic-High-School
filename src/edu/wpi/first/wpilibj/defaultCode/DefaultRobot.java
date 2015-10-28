package edu.wpi.first.wpilibj.defaultCode;
import java.lang.Math;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Jaguar;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Watchdog;
import edu.wpi.first.wpilibj.camera.AxisCamera;
import edu.wpi.first.addons.CANJaguar;

public class DefaultRobot extends IterativeRobot {
	DriverStation m_ds;                     // driver station object
	int m_priorPacketNumber;                // keep track of the most recent packet number from the DS
	int m_dsPacketsReceivedInCurrentSecond;	// keep track of the ds packets received in the current second

        Joystick mainStick;
        Joystick camStick;

	static final int NUM_JOYSTICK_BUTTONS = 16;
	//boolean[] m_rightStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
	//boolean[] m_leftStickButtonState = new boolean[(NUM_JOYSTICK_BUTTONS+1)];
	int m_autoPeriodicLoops;
	int m_disabledPeriodicLoops;
	int m_telePeriodicLoops;
        
        CANJaguar driveFR = new CANJaguar(2);
        CANJaguar driveFL = new CANJaguar(3);
        CANJaguar driveBR = new CANJaguar(4);
        CANJaguar driveBL = new CANJaguar(5);

        AxisCamera ac;

        Servo pan = new Servo(1);
        Servo tilt = new Servo(2);

    public DefaultRobot() {
        System.out.println("BuiltinDefaultCode Constructor Started\n");

		// Acquire the Driver Station object
		m_ds = DriverStation.getInstance();
		m_priorPacketNumber = 0;
		m_dsPacketsReceivedInCurrentSecond = 0;

                mainStick = new Joystick(1);
                camStick = new Joystick(2);
		
		m_autoPeriodicLoops = 0;
		m_disabledPeriodicLoops = 0;
		m_telePeriodicLoops = 0;
                Timer.delay(10.0);
                ac = AxisCamera.getInstance();
                ac.writeResolution(AxisCamera.ResolutionT.k320x240);
                ac.writeBrightness(0);
		System.out.println("BuiltinDefaultCode Constructor Completed\n");
	}


	/********************************** Init Routines *************************************/

	public void robotInit() {
		// Actions which would be performed once (and only once) upon initialization of the
		// robot would be put here.

		System.out.println("RobotInit() completed.\n");
	}

	public void disabledInit() {
		m_disabledPeriodicLoops = 0;// Reset the loop counter for disabled mode
		// Move the cursor down a few, since we'll move it back up in periodic.
	}

	public void autonomousInit() {
		m_autoPeriodicLoops = 0;// Reset the loop counter for autonomous mode
	}

	public void teleopInit() {
		m_telePeriodicLoops = 0;				// Reset the loop counter for teleop mode
		m_dsPacketsReceivedInCurrentSecond = 0;	// Reset the number of dsPackets in current second
	}

	/********************************** Periodic Routines *************************************/
	static int printSec = (int)((Timer.getUsClock() / 1000000.0) + 1.0);
	static final int startSec = (int)(Timer.getUsClock() / 1000000.0);

	public void disabledPeriodic()  {
		// feed the user watchdog at every period when disabled
		Watchdog.getInstance().feed();

		// increment the number of disabled periodic loops completed
		m_disabledPeriodicLoops++;

		// while disabled, printout the duration of current disabled mode in seconds
		if ((Timer.getUsClock() / 1000000.0) > printSec) {
			// Move the cursor back to the previous line and clear it.
			//System.out.println("\x1b[1A\x1b[2K");
			System.out.println("Disabled seconds: " + (printSec - startSec) + "\r\n");
			printSec++;
		}
	}

	public void autonomousPeriodic() {
		// feed the user watchdog at every period when in autonomous
		Watchdog.getInstance().feed();

		m_autoPeriodicLoops++;
	}

        float speed;
        float turn;
        float slide;
        float front_r;
        float front_l;
        float back_r;
        float back_l;

	public void teleopPeriodic() {
            // feed the user watchdog at every period when in autonomous
            Watchdog.getInstance().feed();
            // increment the number of teleop periodic loops completed
            m_telePeriodicLoops++;
            m_dsPacketsReceivedInCurrentSecond++;// increment DS packets received

            pan.set(camStick.getX());
            tilt.set(camStick.getY());

            speed = GetY() * 2;
            turn = GetZ() *  2;
            slide = GetX() * -2;

            driveFR.set(speed-turn+slide);
            driveFL.set((speed+turn-slide) * -1);
            driveBR.set(speed-turn-slide);
            driveBL.set((speed+turn+slide) * -1);
	}

    float GetX() {
        float myX = (float)mainStick.getX();//left right
       
        if(myX < 0.05 && myX > -0.05)
            return 0;
        else
            return (smoothStick(myX) * -1);
        //return myX;
    }

    float GetY() {
        float myX = (float)mainStick.getY();//up down
        
        if(myX < 0.05 && myX > -0.05)
            return 0;
        else
            return myX;
    }
    
    float GetZ() {
        float myX = (float)mainStick.getThrottle();
        
        if(myX < 0.05 && myX > -0.05)
            return 0;
        else
            return ((smoothStick(myX) / 2) * -1);
    }
    /*
    float GetRY() {
        float myX = (float)rightStick.getY();
        return myX;
    }
    */
    float smoothStick(float a)
    {
    	float val = 4.0f * ((0.0000444511f*pow(a,3))-(0.0000279309f*pow(a,2))+(0.295710f*a));
    	
    	if (val < -1.0f) 
    		val = -1.0f; 
    	else if (val > 1.0f) 
    		val = 1.0f;
    	
    	return val;
    }
}
