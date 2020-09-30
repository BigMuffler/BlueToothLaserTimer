#include <SoftwareSerial.h>

#define DETECT 2
#define ACTION 8

int txPin = 1;
int rxPin =0;

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

  if(detected == HIGH)
    {
      digitalWrite(DETECT, HIGH);
      BlueToothConnect.write("A"); 
    }
   if(detected == LOW)
    {   
      digitalWrite(DETECT, LOW);
      BlueToothConnect.write("s");       
           
    } 

  delay(200);
}
