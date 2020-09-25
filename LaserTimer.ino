#include <SoftwareSerial.h>

#define DETECT 2
#define ACTION 8

int txPin = 1;
int rxPin =0;
String data;
int written = 1;

SoftwareSerial BlueToothConnect(rxPin,txPin);
void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);
  BlueToothConnect.begin(9600);
  pinMode(DETECT, INPUT);
  pinMode(ACTION,OUTPUT);

  
}

void loop() {
  // put your main code here, to run repeatedly:    
  int detected = digitalRead(DETECT);

  digitalWrite(ACTION,HIGH);
  
    data = BlueToothConnect.read();

     if(detected == HIGH)
      {
        digitalWrite(DETECT, HIGH);
       
      }
    else
      {   
        digitalWrite(DETECT, LOW);
        BlueToothConnect.write("s");          
        Serial.println("Not");
        written++;

      } 

  delay(200);
}
