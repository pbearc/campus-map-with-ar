import pandas as pd

data = pd.read_csv('mac_addresses.csv')

mac_addresses = []

for index, row in data.iterrows():
    mac_addresses.append(row['mac'])
    
print(mac_addresses)