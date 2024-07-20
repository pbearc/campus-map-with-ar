# import asyncio
# from bleak import BleakScanner
# import pandas as pd
# import joblib

# target_beacons_name = ['Daikin']  # Replace with your actual beacon names

# input_data = [-100] * len(target_beacons_name)  # List to store scanned data

# async def scan():
#     devices = await BleakScanner.discover()
#     for device in devices:
#         if device.name in target_beacons_name:
#             index = target_beacons_name.index(device.name)
#             input_data[index] = device.rssi

# async def main():
#     # Load the trained model
#     clf = joblib.load('trained_model_rf.pkl')
    
#     while True:
#         await scan()
        
#         # Prepare new data for prediction
#         new_data = {name: [rssi] for name, rssi in zip(target_beacons_name, input_data)}
#         new_data_df = pd.DataFrame(new_data)
        
#         # Predict using the loaded model
#         prediction = clf.predict(new_data_df)
#         print(f"Predicted point: {prediction[0]}")
        
#         # Reset input data for next scan
#         input_data[:] = [-100] * len(target_beacons_name)
        
#         # Delay for the next scan
#         await asyncio.sleep(1)  # Scan every 10 seconds

# if __name__ == "__main__":
#     asyncio.run(main())

import asyncio
from bleak import BleakScanner
import numpy as np
from tensorflow.keras.models import load_model
from beacons import target_beacons_name  # Assuming this imports your list of beacon names

# Load your trained neural network model
model = load_model('trained_model.h5')  # Replace with your model file path

# Function to scan for BLE devices and capture RSSI values
async def scan_for_devices():
    devices = await BleakScanner.discover()
    return devices

# Function to extract RSSI values from devices
def extract_rssi(devices):
    rssi_values = {}
    for device in devices:
        if device.name in target_beacons_name:
            rssi_values[device.name] = device.rssi
    return rssi_values

# Main function to predict user's point
async def predict_user_point():
    while True:
        try:
            devices = await scan_for_devices()
            rssi_values = extract_rssi(devices)
            
            # Prepare input data for prediction
            new_data = np.zeros(len(target_beacons_name))
            for i, beacon_name in enumerate(target_beacons_name):
                if beacon_name in rssi_values:
                    new_data[i] = rssi_values[beacon_name]
                else:
                    new_data[i] = -100  # Default value if beacon not detected
            
            # Reshape data for model prediction (if needed)
            new_data = new_data.reshape(1, -1)  # Reshape to (1, num_features)
            
            # Predict the user's point
            prediction = model.predict(new_data)
            predicted_point = np.argmax(prediction)  # Assuming classes are numeric
            
            print(f"Predicted Point: {predicted_point}")
        
        except Exception as e:
            print(f"Error: {e}")
        
        # Delay for the next scan
        await asyncio.sleep(1)  # Adjust as needed

if __name__ == "__main__":
    asyncio.run(predict_user_point())

