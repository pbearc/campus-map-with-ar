import pandas as pd

data = pd.read_csv('points.csv')

points = []

for index, row in data.iterrows():
    # if index==0:
    #     continue
    points.append({"id": row['id'], "name": row['name'], "x": row['x'], "y": row['y']})
    
if __name__ == "__main__":
    print(points)
    print("Data loaded successfully.")

