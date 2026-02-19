#include <SPI.h>
#include <NRFLite.h>

int IN1 = 2;
int IN2 = 3;
int IN3 = 4;
int IN4 = 5;

const static uint8_t RADIO_ID = 0;
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
    Serial.begin(9600);

    pinMode(IN1, OUTPUT);
    pinMode(IN2, OUTPUT);
    pinMode(IN3, OUTPUT);
    pinMode(IN4, OUTPUT);

    pinMode(10, OUTPUT);
    digitalWrite(10, HIGH);

    Serial.println("Starting Receiver...");

    if (!_radio.init(RADIO_ID, PIN_RADIO_CE, PIN_RADIO_CSN)) {
        Serial.println("Cannot communicate with radio");
        while (1);
    }
    
    Serial.println("Receiver Ready!");
}

void loop() {
    while (_radio.hasData()) {
        _radio.readData(&_radioData); 
        
        int command = _radioData.Data;
        Serial.print("Command Received: ");
        Serial.println(command);
        
        moveCar(command);
    }
}

void moveCar(int command) {
    if (command == 0) { 
        digitalWrite(IN1, LOW); digitalWrite(IN2, LOW);
        digitalWrite(IN3, LOW); digitalWrite(IN4, LOW);
    }
    else if (command == 1) { 
        digitalWrite(IN1, HIGH); digitalWrite(IN2, LOW);
        digitalWrite(IN3, HIGH); digitalWrite(IN4, LOW);
    } 
    else if (command == 2) { 
        digitalWrite(IN1, LOW); digitalWrite(IN2, HIGH);
        digitalWrite(IN3, LOW); digitalWrite(IN4, HIGH);
    } 
    else if (command == 3) { 
        digitalWrite(IN1, LOW); digitalWrite(IN2, HIGH); 
        digitalWrite(IN3, HIGH); digitalWrite(IN4, LOW);
    } 
    else if (command == 4) { 
        digitalWrite(IN1, HIGH); digitalWrite(IN2, LOW);
        digitalWrite(IN3, LOW); digitalWrite(IN4, HIGH);
    } 
    
    else if (command == 5) { 
        digitalWrite(IN1, LOW); digitalWrite(IN2, LOW);
        digitalWrite(IN3, HIGH); digitalWrite(IN4, LOW);
    }
    else if (command == 6) { 
        digitalWrite(IN1, HIGH); digitalWrite(IN2, LOW);
        digitalWrite(IN3, LOW); digitalWrite(IN4, LOW);
    }
    else if (command == 7) { 
        digitalWrite(IN1, LOW); digitalWrite(IN2, LOW);
        digitalWrite(IN3, LOW); digitalWrite(IN4, HIGH);
    }
    else if (command == 8) { 
        digitalWrite(IN1, LOW); digitalWrite(IN2, HIGH);
        digitalWrite(IN3, LOW); digitalWrite(IN4, LOW);
    }
}