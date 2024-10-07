const fs = require('node:fs')
const path = require('node:path')
const xlsx = require('xlsx')

const route_map_path = path.join(__dirname, '../main/route')
const routes = fs.readdirSync(route_map_path).filter(file => file.endsWith('.geojson'))
const sup_floor_poi = new Map()
for (const file of routes){
    const file_path = path.join(route_map_path, file)
    const route = JSON.parse(fs.readFileSync(file_path, 'utf8'))
    sup_floor_poi.set(path.basename(file_path, path.extname(file_path)), [])
    for (const feature of route.features){
        sup_floor_poi.get(path.basename(file_path, path.extname(file_path))).push(feature.properties.rep)
    }
}

const base_map_path = path.join(__dirname, '../main/base-map')
const base_maps = fs.readdirSync(base_map_path).filter(file => file.endsWith('.geojson'))
for (const file of base_maps){
    const file_path = path.join(base_map_path, file)
    const map = JSON.parse(fs.readFileSync(file_path, 'utf8'))
    for (const feature of map.features){
        //console.log(feature.properties.id, feature.properties.title, feature.properties.point.coordinates[0], feature.properties.point.coordinates[1], feature.properties.zLevel)
        if (sup_floor_poi.get(path.basename(file_path, path.extname(file_path))) != undefined && sup_floor_poi.get(path.basename(file_path, path.extname(file_path))).includes(feature.properties.id)){
            fetch(`http://127.0.0.1:5000/localization/poi/post`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    'id': feature.properties.id,
                    'name': feature.properties.title,
                    'lat': feature.properties.point.coordinates ? feature.properties.point.coordinates[0] : null,
                    'long': feature.properties.point.coordinates ? feature.properties.point.coordinates[1] : null,
                    'z': feature.properties.zLevel, 
                    'identifier': feature.properties.identifier
                })
            })
        }
    }
}

const loadExcelSheetAsJSON = (filePath, sheetIndex = 0) => {
  const workbook = xlsx.readFile(filePath);
  const sheetName = workbook.SheetNames[sheetIndex];
  const worksheet = workbook.Sheets[sheetName];
  return xlsx.utils.sheet_to_json(worksheet);
};

const beaconDataFilePath = path.join(__dirname, './beaconid.xlsx');
const beaconDataJSON = loadExcelSheetAsJSON(beaconDataFilePath);

beaconDataJSON.forEach(mac => {
    fetch(`http://127.0.0.1:5000/localization/mac/post`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            'address': mac.Address,
            'latitude': mac.Latitude,
            'longitude': mac.Longitude,
            'floor': mac.Floor
        })
    })
})

