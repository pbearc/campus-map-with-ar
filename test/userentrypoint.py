import pandas as pd
from sklearn.ensemble import RandomForestRegressor
import pickle

with open('collectfortest.py') as f:
    code = f.read()
    exec(code)

with open('saved_model.pkl', 'rb') as f:
    model = pickle.load(f)


# Load your newly collected RSSI data
new_data = pd.read_csv('collectedfortest.csv')


# Assuming your RandomForestRegressor model is already trained and stored in 'model'

# Predict coordinates (x and y) using the trained model
y_new_pred = model.predict(new_data)

# Create a DataFrame for the predicted coordinates
predicted_coordinates = pd.DataFrame(y_new_pred, columns=['pred_x', 'pred_y'])

# Print or use the predicted coordinates as needed
print(predicted_coordinates.head())  # Display the first few predictions

# display the mode of predicted coordinates
print(predicted_coordinates.mode())
