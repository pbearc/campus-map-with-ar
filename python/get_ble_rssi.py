import asyncio
from bleak import BleakScanner, AdvertisementData
import time
import pandas as pd
from beacons import target_beacons_name

point = 0



input = []
input_data = [-100]*len(target_beacons_name)
input_data = [point] + input_data


async def scan():
    print("Scanning for 5 seconds...")
    devices = await BleakScanner.discover()
    for device in devices:
        if device.name in target_beacons_name:
            print(f"Point: {point}, Device: {device.name}, Address: {device.address}, RSSI: {device.rssi} dBm")
            index = target_beacons_name.index(device.name)
            input_data[index+1] = device.rssi
            
            
    input.append(input_data)
    print(input)
            # You can also check if the device is an iBeacon and parse its data here

for i in range(3):
    loop = asyncio.get_event_loop()
    loop.run_until_complete(scan())

# with open('output.csv', 'w', newline='') as f:
#     df = pd.DataFrame(input, columns=['Point'] + target_beacon_names)
#     df.to_csv(f, index=False)
    
with open('output.csv', 'a', newline='') as f:
    df = pd.DataFrame(input)
    df.to_csv(f, index=False, header = False)
    





