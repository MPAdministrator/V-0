/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package projectblinky;

/**
 *
 * @author z003tnub
 */
/*
 * #%L
 * **********************************************************************
 * ORGANIZATION  :  Pi4J
 * PROJECT       :  Pi4J :: Java Examples
 * FILENAME      :  ControlGpioExample.java
 *
 * This file is part of the Pi4J project. More information about
 * this project can be found here:  http://www.pi4j.com/
 * **********************************************************************
 * %%
 * Copyright (C) 2012 - 2016 Pi4J
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ProcessBuilder.Redirect;

/**
 * This example code demonstrates how to perform simple state control of a GPIO
 * pin on the Raspberry Pi.
 *
 * @author Robert Savage
 */
public class ControlGpioExample {

    public static void main(String[] args) throws InterruptedException {

        boolean isButtonExample = true;
        if (isButtonExample) {
            customBehaviour();
        } else {
            System.out.println("<--Pi4J--> GPIO Control Example ... started.");
            // create gpio controller
            final GpioController gpio = GpioFactory.getInstance();
            // provision gpio pin #01 as an output pin and turn on
            final GpioPinDigitalOutput pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "MyLED", PinState.HIGH);
            // set shutdown state for this pin
            pin.setShutdownOptions(true, PinState.LOW);
            System.out.println("--> GPIO state should be: ON");
            Thread.sleep(5000);
            // turn off gpio pin #01
            pin.low();
            System.out.println("--> GPIO state should be: OFF");
            Thread.sleep(5000);
            // toggle the current state of gpio pin #01 (should turn on)
            pin.toggle();
            System.out.println("--> GPIO state should be: ON");
            Thread.sleep(5000);
            // toggle the current state of gpio pin #01  (should turn off)
            pin.toggle();
            System.out.println("--> GPIO state should be: OFF");
            Thread.sleep(5000);
            // turn on gpio pin #01 for 1 second and then off
            System.out.println("--> GPIO state should be: ON for only 1 second");
            pin.pulse(1000, true); // set second argument to 'true' use a blocking call
            // stop all GPIO activity/threads by shutting down the GPIO controller
            // (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
            gpio.shutdown();
            System.out.println("Exiting ControlGpioExample");
        }
    }

    static boolean on = false;

    static GpioPinDigitalOutput pin;

    public static void customBehaviour() {
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();
        // provision gpio pin #02 as an input pin with its internal pull down resistor enabled
        final GpioPinDigitalInput myButton = gpio.provisionDigitalInputPin(RaspiPin.GPIO_01, PinPullResistance.PULL_UP);
        pin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_29, "MyLED", PinState.HIGH);
        // set shutdown state for this pin
        pin.setShutdownOptions(true, PinState.LOW);
        // set shutdown state for this input pin
        myButton.setShutdownOptions(true);
        pin.toggle();
        // create and register gpio pin listener

        myButton.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                // display pin state on console
                System.out.println(" --> GPIO PIN STATE CHANGE: " + event.getPin() + " = " + event.getState());
                try {
                    if (event.getState().toString().contains("HIGH")) {
                        connectToBlueTooth();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        System.out.println(" ... complete the GPIO #02 circuit and see the listener feedback here in the console.");
        // keep program running until user aborts (CTRL-C)
        try {
            while (true) {
                Thread.sleep(500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void lightLed(boolean toSet) {
        if (toSet) {
            on = true;
            System.out.println("Pin is Blink");
            pin.blink(100);
        } else {
            on = false;
            //pin.setState(false);
            System.out.println("Pin is not Blink");
            pin.blink(1, 1);
            pin.setState(PinState.LOW);
            //pin.toggle();

        }
    }

    public static void connectToBlueTooth() throws IOException, InterruptedException {
        if (on == false) {
            lightLed(true);
//            ProcessBuilder pb = new ProcessBuilder("sudo", "bash", "-c", "cd ~ ; "
//                    + "hciconfig hci0 up ; "
//                    + "hciconfig hci0 sspmode 1 ; "
//                    + "hciconfig hci0 piscan ; "
//                    + "bluetooth-agent 1234 ; ");

            ProcessBuilder pb = new ProcessBuilder("sudo", "bash", "-c", "cd ~ ; "
                    + "sudo bluetoothctl ; "
                    + "power on ; "
                    + "discoverable on ; "
                    + "pairable on ; "
                    + "agent NoInputNoOutput ; "
                    + "default-agent ; " + "EOF ; ");
            System.out.println("Starting");
            
            pb.redirectOutput(Redirect.INHERIT);
            pb.redirectError(Redirect.INHERIT);
            
            Process process = pb.start();

            Runnable r = new Runnable() {
                public void run() {
                    try {
                        int errCode = process.waitFor();
                        System.out.println("Echo command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
                        System.out.println("Error Code: " + errCode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            };

            Thread t = new Thread(r);
            t.start();
            Thread.sleep(30000);

            if (t.isAlive()) {
                System.out.println("t is alive");
                t.interrupt();
                System.out.println("Had to interupt");
            } else if (t.isInterrupted()) {
                System.out.println("t is interuppted");
            } else {
                System.out.println("I have no clue what the fuck is up. Fuck This, fuck me, and fuck you too");
            }

            lightLed(false);
        }
    }

    public static void connectToWifi() throws Exception {

        String networkName = "BadBoiTest2";
        String networkPass = "Password";
        System.out.println(System.getProperty("os.name"));

        ProcessBuilder pb = new ProcessBuilder("sudo", "bash", "-c", "cd ~ ; "
                + "ip link set wlan0 up ; "
                //                + "sudo rm /etc/wpa_supplicant.conf ; "
                //+ "touch /etc/wpa_supplicant.conf ; "
                + "wpa_passphrase " + networkName + " " + networkPass + " >> /etc/wpa_supplicant/wpa_supplicant.conf ; "
                //                + "sudo rm /etc/wpa_supplicant.conf ; "
                + "cat /etc/wpa_supplicant.conf/wpa_supplicant.conf ; "
                + "sudo wpa_supplicant -B -D nl80211 -i wlan0 -c /etc/wpa_supplicant/wpa_supplicant.conf ; "
                + "/sbin/iw wlan0 link ; "
                + "reboot ; ");

        System.out.println("Starting");
        Process process = pb.start();
        int errCode = process.waitFor();
        System.out.println("Echo command executed, any errors? " + (errCode == 0 ? "No" : "Yes"));
        System.out.println("Error Code: " + errCode);
        //System.out.println("Echo Output:\n" + output(process.getInputStream()));
    }

    private static String output(InputStream inputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(inputStream));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                sb.append(line + System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        return sb.toString();
    }
}
