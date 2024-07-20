## Plans for now:

1. Wait for beacons to arrive
2. Wait for android device (rent it from campus, or buy a cheap one)
3. Test the android app BLE scanning feature
4. Offline phase: Try to fingerprint BLE devices and their RSSI values (if we're using android, I shall implement more features in this app to record RSSI values to database, or if there's any better way to do it, PLEASE SUGGEST)
5. Find suitable algorithm for online phase, I'm not sure how they do it on android, so far I only know tensor flow can be used in android studio, idk how others make it work with different model (PLEASE SUGGEST)
6. Pray that online phase works!!!
7. Request for permissions to test it on campus (diff location, 1 or 2 corridor will do)
8. Pass to update prediction result in maze map, do routing and all
9. Implement AR features

# android folder

This folder contains the Android app for bluetooth scanning feature.
It is supposed to scan BLE instead of normal bluetooth class.
Unfortunately without the beacons I'm unable to test whether it fully works or not.
We also need android model>=11 to run this app. (Emulator can't detect BLE)

Need:
1. Beacons
2. Android Device

# python folder (most probably won't be using..?)

This folder contains the python code for offline phase and online phase.
I'm 80% unsure about the code, since it takes a long time to run and I'm unable to test it.
I experimented with pybluez2 and bleak library, both are returning different devices. (so double unsure)
I assume pybluez2 finds normal bluetooth whereas bleak scans BLE.
Or for more accuracy we should do offline phase in an android app itself!! I thought of this only after writing this code lol.

