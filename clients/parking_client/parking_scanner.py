#! python
import argparse
import random                   # random heart rate time series
import string
import time                     # timestaping messages                  # for data interpolation in time series
import json                     # for message formatting
import paho.mqtt.client as mqtt # for MQTT communication
import sys
from paho.mqtt.client import Client

#####
## Constants Declarations for communication
#####
path = "data.json"
path1 = "qr.json"
MQTT_TOPIC_TRANSPONDER = 'transponder'
MQTT_TOPIC_QRCODE = 'qrcode'
MQTT_TOPIC_BUTTON= 'buttonClick'
MQTT_BROKER_PORT = 1883

VALID_TRANSPONDER = "980ac437e84834be"
INVALID_TRANSPONDER = "338b3c6b10aa551b"

VALID_EXIT = "EXIT12345"
INVALID_EXIT = "EXIT22345"
VALID_ENTRY = "ENTRY12345"
INVALID_ENTRY = "ENTRY12346"

TRANSPONDER_CASE1 = {
    "transponderNumber": VALID_TRANSPONDER,
    "gateNumber": VALID_ENTRY,  # true for entrance
}
TRANSPONDER_CASE2 = {
    "transponderNumber": INVALID_TRANSPONDER,
    "gateNumber": VALID_ENTRY,  # true for entrance
}
TRANSPONDER_CASE3 = {
    "transponderNumber": VALID_TRANSPONDER,
    "gateNumber": VALID_EXIT,  # true for entrance
}
TRANSPONDER_CASE4 = {
    "transponderNumber": INVALID_TRANSPONDER,
    "gateNumber": VALID_EXIT,  # true for entrance
}
QR_CASE1 = {
    "qrCode": INVALID_TRANSPONDER,
    "gate": VALID_ENTRY,  # true for entrance
}
QR_CASE2 = {
    "qrCode": INVALID_TRANSPONDER,
    "gateNumber": VALID_EXIT,  # true for entrance
}
LP_CASE1 = {
    "licensePlate": INVALID_TRANSPONDER,
    "gateNumber": VALID_ENTRY,  # true for entrance
}
LP_CASE2 = {
    "licensePlate": INVALID_TRANSPONDER,
    "gateNumber": VALID_EXIT,  # true for entrance
}
def send_transponder(client:mqtt.Client, transponder:dict ) -> None:
    new_parker = {}
    with open(path, 'r') as f:
        try:
            new_parker = json.load(f)
        except json.JSONDecodeError as e:
            print(f"JSON decode error: {e}")
        client.publish(MQTT_TOPIC_TRANSPONDER, json.dumps(transponder), qos=1)
        if new_parker.get(transponder["transponderNumber"]) is None:
            new_parker[transponder["transponderNumber"]] = transponder
    with open(path, 'w') as json_file:
        json.dump(new_parker, json_file, indent=4) # temporarily save the info

def send_qr(client:mqtt.Client, qr:dict ) -> None:
    new_parker = {}
    with open(path1, 'r') as f:
        try:
            new_parker = json.load(f)
        except json.JSONDecodeError as e:
            print(f"JSON decode error: {e}")
        client.publish(MQTT_TOPIC_QRCODE, json.dumps(qr), qos=1)
        if new_parker.get(qr["qrCode"]) is None:
            new_parker[qr["qrCode"]] = qr
    with open(path1, 'w') as json_file:
        json.dump(new_parker, json_file, indent=4) # temporarily save the info


def send_button(client:mqtt.Client, button:dict ) -> None:
    new_parker = {}
    with open(path1, 'r') as f:
        try:
            new_parker = json.load(f)
        except json.JSONDecodeError as e:
            print(f"JSON decode error: {e}")
        client.publish(MQTT_TOPIC_BUTTON, json.dumps(button), qos=1)
        if new_parker.get(button["licensePlate"]) is None:
            new_parker[button["licensePlate"]] = button
    with open(path1, 'w') as json_file:
        json.dump(new_parker, json_file, indent=4) # temporarily save the info

#####
# MQTT Communication Layer
#####

def mqtt_init(identifier, receiver, port):
    client = mqtt.Client(mqtt.CallbackAPIVersion.VERSION2, identifier)
    client.on_publish = on_publish
    client.connect(receiver, port, 60)
    return client

def on_connect(client, userdata, flags, reason_code, properties):
    if reason_code.is_failure:
        print(f"Failed to connect: {reason_code}")
    else:
        print(f"Connection established.")

def on_publish(client, userdata, mid, reason_code, properties):
    try:
        pass
    except KeyError:
        print("An error has occured " + reason_code)

if __name__ == "__main__":
    #parser = argparse.ArgumentParser(description="Create some parking info")
    #parser.add_argument("function_name", type=str, help="Name of the function to execute (one, two, or three)")
    #parser.add_argument("param", type=int, help="Second parameter (integer)")
    #args = parser.parse_args()
    try:
        #(sid, receiver) = process_cli()
        sid = "scanner"
        receiver = "localhost"
        client = mqtt_init(sid, receiver, MQTT_BROKER_PORT)
        client.loop_start()
        send_transponder(client, TRANSPONDER_CASE1)
        #send_qr(client, QR_CASE1)
        #send_button(client, LP_CASE1)
    except ConnectionRefusedError:
        print('Connection refused (is broker alive?)')
    except KeyboardInterrupt:
        print('\n\n ... Stoping device ...\n')
        client.loop_stop()
        sys.exit(0)
