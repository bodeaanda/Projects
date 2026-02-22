# Gesture Controlled Car 

#Description

This project is a wireless, gesture-controlled car that can be maneuvered entirely through hand movements. By simply tilting your hand, the car can move forward, backward, left and right. It leverages and MPU6050 sensor (accelerometer and gyroscope) to detect your hand's movement and an nRF24L01 transceiver module to wirelessly send the gesture commands to the car.

## Technologies and Components used

### Hardware
- **Arduino Nano R3** (used for the Transmitter)
- **Arduino UNO** (used for the Receiver)
- **MPU6050** (6-axis Accelerometer + Gyroscope sensor)
- **nRF24L01 Module** (2.4GHz Wireless Transceiver)
- **L298N Motor Driver** (controls the DC motors)
- **12V DC Motors** (used for car wheels)
- **9V Batteries** (power supply)
- **Breadboard & Jumper Wires (used for assembling both circuits)

### Software
- **Arduino IDE** 

## How it works
The system is divided into two distinct parts: the **Transmitter (Hand Controller)** and the **Receiver (Car)**.

### 1. Transmitter (Hand Controller)
The transmitter acts as te remote control and typically rests on the user's hand (like a glove). It consists of an Arduino Nano and the MPU6050 sensor. The MPU6050 continuously measures the tilt of the hand by generating X, Y and Z-axis data.
Based on these tilt values (acceleration bounds), the Arduino determines the intended direction.
The mapped command values are then packed and transmitted wirelessly via the nRF24L01 radio module.

### 2. Receiver (Car)
The receiver is mounted on the car chassis and includes an Arduino UNO, an nRF24L01 receiver module, an L298N motor driver and DC motors.
The nRF24L01 module constantly listens for incoming packets from the transmitter. Once a command is interpreted by the Arduino, it applies the appropriate logic to the L298N motor driver pins, making the motors rotate in paths correlated to the hand movement.