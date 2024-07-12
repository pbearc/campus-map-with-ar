import csv
import time
import os
from collections import defaultdict
from pywifi import PyWiFi, const, Profile
from loadpoints import points
from mac_addresses import mac_addresses

    # Add more points as needed
    
point = points[0]  # Change this to the point you are collecting data for

data_file = "new_corridor1.csv" 

wifi = PyWiFi()
iface = wifi.interfaces()[0]  # Get the first wireless interface|

# def scan():
#     iface.scan()
#     time.sleep(2)  # Wait for the scan to complete
#     results = iface.scan_results()
#     return results


def scan():
    iface.scan()
    max_wait_time = 5  # Maximum time to wait for the scan to complete in seconds
    sleep_interval = 0.1  # Interval to wait between checks in seconds
    start_time = time.time()
    
    while time.time() - start_time < max_wait_time:
        results = iface.scan_results()
        if results:
            return results
        time.sleep(sleep_interval)
    
    # If the scan didn't complete within the max wait time, return what we have
    return iface.scan_results()

# Print the current working directory
print(f"Current working directory: {os.getcwd()}")

row = defaultdict(list)

with open(data_file, mode='a', newline='') as file:
    writer = csv.writer(file)
    header = ["id", "name", "x", "y"] + mac_addresses
    writer.writerow(header)
    
    # collect data for certain time period
    
    for i in range(10):
        cells = scan()
        for cell in cells:
            if cell.bssid in mac_addresses:
                row[cell.bssid].append(cell.signal)
        time.sleep(1)
        
    # print(row)
    
    for mac_address in mac_addresses:
        if mac_address not in row:
            print(mac_address, "not found")
            row[mac_address].extend([-100] * 20)
        elif row[mac_address]:
            while len(row[mac_address]) < 20:
                row[mac_address].append(-100)
            
    
    for i in range(20):
        this_row = [point["id"], point["name"], point["x"], point["y"]]
        for mac_address in mac_addresses:
            this_row.append(row[mac_address][i])
            
        writer.writerow(this_row)
        
    print(f"Data collected for point {point['name']}, {this_row}")

print(f"Data collection complete. The data is saved in {data_file}")
