# import tensorflow as tf

# # Load your Keras model
# model = tf.keras.models.load_model('trained_model.h5')

# # Convert the Keras model to TensorFlow Lite format
# converter = tf.lite.TFLiteConverter.from_keras_model(model)
# tflite_model = converter.convert()

# # Save the TensorFlow Lite model
# with open('model.tflite', 'wb') as f:
#     f.write(tflite_model)

import asyncio
from bleak import BleakScanner

async def scan_ble():
    print("Scanning for BLE devices...")
    devices = await BleakScanner.discover()
    for device in devices:
        print(f"Device Name: {device.name}, Address: {device.address}, RSSI: {device.rssi}")

loop = asyncio.get_event_loop()
loop.run_until_complete(scan_ble())

