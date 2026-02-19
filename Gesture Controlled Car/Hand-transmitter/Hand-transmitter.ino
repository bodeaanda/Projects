#include <Wire.h>
#include <TinyMPU6050.h>
#include <SPI.h>
#include <NRFLite.h>

MPU6050 mpu (Wire);

int message;
const static uint8_t RADIO_ID = 1;             
const static uint8_t DESTINATION_RADIO_ID = 0; 
const static uint8_t PIN_RADIO_CE = 7;
const static uint8_t PIN_RADIO_CSN = 8;

struct RadioPacket { 
  uint8_t FromRadioId;
  uint32_t Data;
  uint32_t FailedTxCount;
};

NRFLite _radio;
RadioPacket _radioData;

void setup() {
  pinMode(LED_BUILTIN, OUTPUT);
  
  digitalWrite(LED_BUILTIN, HIGH);
  
  mpu.Initialize();
  mpu.Calibrate(); 
  
  digitalWrite(LED_BUILTIN, LOW);
  delay(500);

  if (!_radio.init(RADIO_ID, PIN_RADIO_CE, PIN_RADIO_CSN)) {
    while (1) {
      digitalWrite(LED_BUILTIN, HIGH);
      delay(100);
      digitalWrite(LED_BUILTIN, LOW);
      delay(100);
    }
  }
  
  for(int i=0; i<3; i++) {
    digitalWrite(LED_BUILTIN, HIGH);
    delay(300);
    digitalWrite(LED_BUILTIN, LOW);
    delay(300);
  }

  _radioData.FromRadioId = RADIO_ID;
}

void loop() {
  mpu.Execute();
  
  long x = mpu.GetRawAccX();
  long y = mpu.GetRawAccY();
  long t = 6000; 

  message = 0; 
  if (x >= t) { 
      if (y >= t)       message = 7;
      else if (y <= -t) message = 8;
      else              message = 2;
  }
  
  else if (x <= -t) { 
      if (y >= t)       message = 5;
      else if (y <= -t) message = 6;
      else              message = 1;
  }
  
  else { 
      if (y >= t)       message = 4;
      else if (y <= -t) message = 3;
      else              message = 0; 
  }

  _radioData.Data = message;
  
  if (_radio.send(DESTINATION_RADIO_ID, &_radioData, sizeof(_radioData))) {
  } else {
      _radioData.FailedTxCount++;
  }

  delay(50); 
}