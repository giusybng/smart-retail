# Smart Retail
This is an Android application *(Java)* to optimize a user's shopping experience in a supermarket. The app allows the user to create a shopping list and guide him inside the supermarket, making him make as few trips as possible. Each supermarket aisles is represented by a beacon. When the user is near the beacon, the app suggests which of the products on his list he can find there.

The server is written in *Node.js* while it was used *MongoDB* to store the supermarket products list in JSON format.

The Android application has been tested with *iBeacon*.

## Installation
**Setup MongoDB**

Inside the server folder:

```> mongoimport --db smart-retail --collection products --file Products.json --jsonArray```

NOTE: BeaconId in [Products.json](https://github.com/giusybng/smart-retail/blob/main/server/Products.json) is the integer hash of the beacon and not UUID because I noticed that the two beacons used had the same UUID but different 'major' and 'minor'.

**Setup server**
Inside the server folder: 

```> npm install```

```> npm start```

**Setup SmartRetail *(Android application)***
- Change the server URL inside the file [BeaconResultsActivity.java](https://github.com/giusybng/smart-retail/blob/main/SmartRetail/app/src/main/java/com/bongiovanni/smartretail/BeaconResultsActivity.java)
- Build and run app

### Android version
*API 16: Android 4.1 (Jelly Bean)*
