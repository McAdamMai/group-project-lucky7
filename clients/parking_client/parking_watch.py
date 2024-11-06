import random
import time
import datetime
import sys
import argparse
import json
import paho.mqtt.client as mqtt
import socket

MQTT_TOPIC_NAME  = 'tmt_parking'
MQTT_BROKER_PORT = 1883

transponder_ids = [
    "VALID12345", "INVALID67890", "VALID54321",
    "INVALID00000", "VALID11111", "VALID67890", "INVALID54321"
]

def start():
    print('**********')
    print('* Target receiver: ' + str(client))
    print('**********')
    print(' ... press ^C to stop the device ...\n')

    while(True):
        send_sequence()

def send_sequence():
    transponder_id = random.choice(transponder_ids)
    timestamp = time.time()
    send_data(transponder_id, timestamp)

def send_data(id, timestamp):   
    date = datetime.datetime.fromtimestamp(timestamp)
    print(str(date) + ': ' + str(id))
    data = { "t": timestamp, "n": id, "v": "transponder_id" }
    client.publish(MQTT_TOPIC_NAME, json.dumps(data), qos = 1)
    time.sleep(20)

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
        print("An error has occurred " + reason_code)

def process_cli():
    parser = argparse.ArgumentParser()
    parser.add_argument("receiver", help="Address of the receiver queue")
    args = parser.parse_args()
    return args.receiver

if __name__ == "__main__":
    try:
        receiver = process_cli()
        client = mqtt_init("parking_client", receiver, MQTT_BROKER_PORT)
        client.loop_start()
        start()
    except socket.gaierror:
        print('Socket error (wrong receiver address?)')
    except ConnectionRefusedError:
        print('Connection refused (is broker alive?)')
    except KeyboardInterrupt:
        print('\n\n ... Stoping device ...\n')
        client.loop_stop()
        sys.exit(0)