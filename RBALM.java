import java.lang.Math;
import java.util.ArrayList;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.lang.Exception;
import java.net.NXTSocketUtils;
import java.net.Socket;
import javax.bluetooth.RemoteDevice;

import lejos.nxt.comm.NXTConnection;
import lejos.nxt.Motor;
import lejos.nxt.MotorPort;
import lejos.nxt.NXTMotor;
import lejos.nxt.NXTRegulatedMotor;
import lejos.nxt.TouchSensor;
import lejos.nxt.SensorPort;
import lejos.nxt.Button;
import lejos.nxt.LCD;
import lejos.util.Delay;
import lejos.nxt.comm.BTConnection;
import lejos.nxt.comm.Bluetooth;

public class RBALM {
	private DataInputStream ins;
  private DataOutputStream outs;
	private BTConnection btc = null;
	private Socket sock = null;

  public static void main (String[] args) { 
    System.out.println("Starting Program...");
    new RBALM();
  }

  public RBALM() {
  	NXTRegulatedMotor xMotor = Motor.A;
  	NXTRegulatedMotor yMotor = Motor.B;
    NXTMotor handMotor = new NXTMotor(MotorPort.C);
    TouchSensor touch = new TouchSensor(SensorPort.S1);
  	int maxSpeed = (int) xMotor.getMaxSpeed();
  	int multFactor = maxSpeed / 100;
		try {
			connect();
		} catch (Exception e) {
			System.out.println("Failed to connect");
      System.out.println(e);
      Delay.msDelay(10000);
			System.exit(1);
		}
		while(true){
			try {
        if (Button.ENTER.isDown()) {
          xMotor.rotateTo(0);
          yMotor.rotateTo(0);
          System.exit(1);
        }
				ins = new DataInputStream(sock.getInputStream());
        outs = new DataOutputStream(sock.getOutputStream());
				String input = readLine();
        outs.writeChars("0\n");
        outs.flush();
  			ArrayList<String> parts = split(input, ":");
        int handBool = Integer.parseInt(parts.get(0));
        int xPow = Integer.parseInt(parts.get(1)) * -1;
        int yPow = Integer.parseInt(parts.get(2));
        //System.out.println(handBool + ":" + xPow + ":" + yPow);
        int power = 50;
  			if (!touch.isPressed())//(handBool == 1)
        { handMotor.setPower(power * -1); } else { while(touch.isPressed()) { handMotor.setPower(power); } }
        int xAngle = angle(xMotor, xPow, -425, 425);
        int yAngle = angle(yMotor, yPow, -240, 180);
        System.out.println(xAngle + ", " + yAngle);
			} catch(Exception e) {
        System.out.println(e.getMessage());
        Delay.msDelay(2000);
      }
		}
		//ins.close();
		//sock.close();
  }

  public int angle(NXTRegulatedMotor motor, int pow, int min, int max) {
    int middle = (int) (min + max) / 2;
    int diff = Math.abs(max - min);
    double halfDiff = diff / 2;
    double multiplyer = halfDiff / 100.0;
    int angle = (int) (((double) pow) * multiplyer);
    angle += middle;
    motor.rotateTo(angle, true);
    return angle;
  }

  public void power(NXTRegulatedMotor motor, int pow, int multFactor) {
  	motor.setSpeed(Math.abs(pow) * multFactor);
  	if (pow > 0) {
  		motor.forward();
  	} else if (pow < 0) {
  		motor.backward();
  	}
  }

  public ArrayList<String> split(String input, String splitter) throws Exception {
  	char[] charArray = input.toCharArray();
  	ArrayList<String> parts = new ArrayList<String>();
    int index = 0;
  	while (index < charArray.length) {
  		String s = "";
  		while (index < charArray.length) {
  			char c = charArray[index];
  			index++;
  			if (c == ':') break;
  			s += c;
  		}
  		parts.add(s);
  	}
  	return parts;
  }

	public void connect() throws Exception {
		System.out.println("Waiting...");
		btc = Bluetooth.waitForConnection();
    System.out.println("Setting NXT connection...");
		NXTSocketUtils.setNXTConnection(btc);
    System.out.println("Creating socket...");
		sock = new Socket("localhost", 8081);
		System.out.println("Connected");
	}

	private String readLine() throws Exception {
		String s = "";
		while(true) {
			char c = (char) ins.readByte();
			if (c == '\n') break;
      s = s + c;
		}
		return s;
	}
}