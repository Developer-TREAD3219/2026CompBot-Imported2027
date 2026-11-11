package frc.robot.subsystems;

import frc.robot.Constants.LedConstants;
import frc.robot.Constants.States;
import org.wpilib.hardware.led.AddressableLED;
import org.wpilib.hardware.led.AddressableLEDBuffer;
import org.wpilib.hardware.led.AddressableLEDBufferView;
import org.wpilib.hardware.led.LEDPattern;
import org.wpilib.system.RobotController;
import org.wpilib.hardware.led.LEDPattern.GradientType;
import org.wpilib.util.Color;
import org.wpilib.command2.Command;
import org.wpilib.command2.SubsystemBase;
import static org.wpilib.units.Units.*;

import java.util.HashMap;
import java.util.Map;

import org.wpilib.units.measure.Distance;

/* 
 * TreadLeds provides different LED colors/patterns to indicate the state of
 * robot.
 */

public class LEDSubsystem extends SubsystemBase {
    private static final int kPort = LedConstants.kPwmPort;
    private static final int kLength = LedConstants.kTotalLeds; // 60 LEDs per Meter for 2.74 meters (27in x 4)

    private final AddressableLED m_led;
    private final AddressableLEDBuffer m_buffer;
    @SuppressWarnings("unused")
    private final AddressableLEDBufferView m_FrontView;
    @SuppressWarnings("unused")
    private final AddressableLEDBufferView m_RightView;
    @SuppressWarnings("unused")
    private final AddressableLEDBufferView m_LeftView;
    @SuppressWarnings("unused")
    private final AddressableLEDBufferView m_BackView;

    Distance kLedSpacing = Meters.of(1 / 120.0);
    LEDPattern m_greenGold = LEDPattern.gradient(GradientType.kDiscontinuous, new Color(256, 0, 0),
            new Color(256, 256, 0));
    LEDPattern m_scrollingGreenGold = m_greenGold.scrollAtAbsoluteVelocity(MetersPerSecond.of(1), kLedSpacing);

    private States m_currentState;
    Map<States.State, LEDPattern> m_patternMap = new HashMap<States.State, LEDPattern>();
    boolean m_UserButton = false;

    public LEDSubsystem(States robotState) {
        m_led = new AddressableLED(kPort);
        m_buffer = new AddressableLEDBuffer(kLength);
        int viewLength = kLength / 4; // a view for each side of the robot
        m_FrontView = m_buffer.createView(0, viewLength - 1);
        m_RightView = m_buffer.createView(viewLength, viewLength * 2 - 1);
        m_LeftView = m_buffer.createView(viewLength * 2, viewLength * 3 - 1);
        m_BackView = m_buffer.createView(viewLength * 3, viewLength * 4 - 1);
        m_led.setLength(m_buffer.getLength());
        m_led.setData(m_buffer);
        /*
        
2. Why .start() is Missing in 2027

• Automatic / Buffer-Driven Updates: In the 2027 Systemcore HAL (wpi/hal/AddressableLED.h), the driver manages buffer locations (HAL_SetAddressableLEDStart) and pushes updates directly when setData(buffer) is called, rather than requiring a manual background DMA loop toggle via .start().
• Turning Off LEDs: To turn off the strip without .stop(), write a cleared/black AddressableLEDBuffer (all RGB values set to 0) via setData(), or close/dispose of the object when shutting down.
 */
        m_led.start();
        m_currentState = robotState;

        initializeLedPatterns();

        LEDPattern pattern = m_patternMap.get(m_currentState.getState());
        pattern.applyTo(m_buffer);
        m_led.setData(m_buffer);
        setDefaultCommand(runPattern(LEDPattern.solid(Color.BLACK)).withName("Off"));
    }

    private void initializeLedPatterns() {
        for (States.State state : States.State.values()) {
            LEDPattern pattern;
            switch (state) {
                case Initial:
                    pattern = getTread3219(); // Go Green and Gold!
                    break;
                case Autonomous:
                    pattern = getSolidColor(0, 0, 255); // Blue
                    break;
                case TeleopInPosition:
                    pattern = getSolidColor(255, 0, 0); // Green
                    break;
                case Launching:
                    pattern = getBlinkingColor(0, 255, 0); // Red
                    break;
                case Fueling:
                    pattern = getBlinkingColor(0, 255, 255); // Cyan
                    break;
                case TargetAcquired:
                    pattern = getBlinkingColor(255, 255, 0); // Yellow
                    break;
                case NoTarget:
                    pattern = getBlinkingColor(255, 0, 0); // Red
                    break;
                case Climbing:
                    pattern = getBlinkingColor(0, 0, 255); // Red
                    break;
                default:
                    pattern = getSolidColor(0, 0, 0); // Off
            }
            m_patternMap.put(state, pattern);
        }
    }

    @Override
    public void periodic() {
        if (RobotController.getUserButton()) {
            m_UserButton = true;
        } else if (!RobotController.getUserButton() && m_UserButton) {
            // Executed when the user button is released.
            // This allows us to test the different lighting configurations.
            m_currentState.incrementState();
            System.out.println("Changing to state: " + m_currentState.getState());
            System.out.println("Size of map" + m_patternMap.size());
            m_UserButton = false;
        }
        // System.out.println("Current State: " + m_currentState.getState());
        m_patternMap.get(m_currentState.getState()).applyTo((m_buffer));
        m_led.setData(m_buffer);
    }

    /**
     * Creates a command that runs a pattern on the entire LED strip.
     *
     * @param pattern the LED pattern to run
     */
    public Command runPattern(LEDPattern pattern) {
        return run(() -> pattern.applyTo(m_buffer));
    }

    private LEDPattern getTread3219() {
        return m_scrollingGreenGold;
    }

    private LEDPattern getBlinkingColor(int r, int g, int b) {
        LEDPattern base = LEDPattern.solid(new Color(r, g, b));
        LEDPattern blinkColor = base.blink(Seconds.of(0.5), Seconds.of(0.5));
        return blinkColor;
    }

    private LEDPattern getSolidColor(int r, int g, int b) {
        LEDPattern rgbColor = LEDPattern.solid(new Color(r, g, b));
        return rgbColor;
    }
}
