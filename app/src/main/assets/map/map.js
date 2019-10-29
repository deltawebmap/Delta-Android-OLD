var map = {};

map.updateSavedPos = function() {
    var p = map.map.getCenter();
    native.MapOnUpdateSavedPos(p.lat.toString(), p.lng.toString(), map.map.getZoom().toString());
}

map.dino_marker_list_index = 100;

map.init = function(settings) {
    //Create map
    map.map = L.map('map_part', {
        crs: L.CRS.Simple,
        minZoom: 0,
    }).setView([settings.pos.x, settings.pos.y], settings.pos.z);

    //Set background color
    map.setBackground(settings.bg);

    //Add events
    map.map.addEventListener('moveend', map.updateSavedPos);
    map.map.addEventListener('zoomend', map.updateSavedPos);

    //Set the game map bg
    L.tileLayer(settings.map, {
        attribution: 'Studio Wildcard',
        maxNativeZoom: 5,
        maxZoom:12,
        id: 'ark_map',
        opacity: 1,
        zIndex: 1,
        bounds:map.getBounds()
    }).addTo(map.map);

    //Set var
    map.mapName = settings.map;
    map.mapData = settings.mapData;

    //Let the map know that we are ready
    native_engine.JsIsPrepared();
};

map.addDinos = function(data) {
    //Add all dinos
    for(var i = 0; i<data.dinos.length; i+=1) {
        map.addDinoMarker(data.dinos[i]);
    }

    //Add structures
    //map.enableStructuresLayer(data.structures, map.mapData.sourceImageSize * map.mapData.pixelsPerMeter);
}

map.addDinoMarker = function(dino) {
    var icon_size = 40;
    var icon = L.icon({
        iconUrl: "blank_50px.png",
        shadowUrl: null,
    
        iconSize:     [icon_size, icon_size], // size of the icon
        shadowSize:   [0, 0], // size of the shadow
        iconAnchor:   [icon_size/2, icon_size/2], // point of the icon which will correspond to marker's location
        shadowAnchor: [icon_size/2, icon_size/2],  // the same for the shadow
        popupAnchor:  [icon_size/2, icon_size/2] // point from which the popup should open relative to the iconAnchor
    });
    //Add to map
    var pos = map.convertFromNormalizedToMapPos(dino.adjusted_map_pos);

    var index = map.dino_marker_list_index;
    
    var dino_icon = L.marker(pos, {
        icon: icon,
        zIndexOffset:index+1
    }).addTo(map.map);

    //Add items
    dino_icon.x_dino_url = dino.apiUrl;
    dino_icon.x_dino_data = dino;

    //Add events
    dino_icon.on('click', map.onDinoClicked);
    
    //Set real image
    map.createBackground(dino_icon._icon, dino.imgUrl);
}

map.createBackground = function(e, imgUrl) {
    e.style.background = "url('"+imgUrl+"'), white";
    e.style.backgroundRepeat = "no-repeat";
    e.style.backgroundPositionX = "center";
    e.style.backgroundPositionY = "center";
    e.style.border = "2px solid black";
    e.style.borderRadius = "40px";
    e.style.backgroundSize = "30px";
}

map.setBackground = function(color) {
    document.getElementById('map_part').style.backgroundColor = color;
}

map.convertFromNormalizedToMapPos = function(pos) {
    ///This map is weird. 0,0 is the top right, while -256, 256 is the bottom right corner. Invert x
    return [
        (-pos.y * 256),
        (pos.x * 256)
    ];
}

map.convertFromMapPosToNormalized = function(pos) {
    ///This map is weird. 0,0 is the top right, while -256, 256 is the bottom right corner. Invert x
    return [
        (-pos.y / 256),
        (pos.x / 256)
    ];
}

map.getBounds = function() {
    return [
        [-256, 0],
        [0, 256]
    ];
}

map.onDinoClicked = function() {
    //Call native
    var url = this.x_dino_url;
    native.MapOnDinoClicked(url);
}

map.doGoToPos = function(data) {
    var p = map.convertFromNormalizedToMapPos(data);
    map.map.flyTo(L.latLng(p[0], p[1]));
}

//Structures
map.structuresScale = {
    "TheCenter":800,
    "Extinction":2000
}

map.createDom = function(type, classname, parent) {
    var e = document.createElement(type);
    e.className = classname;
    if(parent != null) {
        parent.appendChild(e);
    }
    return e;
}

map.enableStructuresLayer = function(structures, mapSize) {
    var CanvasLayer = L.GridLayer.extend({
        createTile: function(coords){
            // create a <canvas> element for drawing
            var tile = L.DomUtil.create('div', 'leaflet-tile structures_layer');
            console.log("warning: recreating tiles");

            // setup tile width and height according to the options
            var size = this.getTileSize();
            /*ft.style.width = size.x;
            ft.style.height = size.y;

            //Create tile where things will be placed
            var tile = ark.createDom("div", "structures_layer", ft);*/
            tile.style.width = size.x;
            tile.style.height = size.y;

            //Add structures
            for(var i = 0; i<structures.length; i+=1) {
                var s = structures[i];
                if(s.priority != 1 && s.priority != 10 && s.priority != 11) {
                    continue;
                }
                var t = map.createDom("div", "structures_item", tile);
                t.style.left = (s.map_pos.x - 4).toString() + "%";
                t.style.top = (s.map_pos.y - 4).toString() + "%";
                t.style.backgroundImage = "url('"+s.imgUrl+"')";
                t.style.transform = "scale("+(s.ppm/map.structuresScale[map.mapName]).toString()+") rotate("+s.rot.toString()+"deg)";
                t.style.zIndex = (s.priority+100).toString();
            }

            //This is such an ugly hack.
            window.setTimeout(function() {
                //This is called AFTER the transform is applied, so we can apply our own. Ew.
                var scale = 256 / mapSize;
                tile.style.transform+=" scale("+scale.toString()+")"; //I hate myself for writing this line
            }, 50);
            return tile;
        }
    });
    var l = new CanvasLayer({
        maxNativeZoom: 0,
        maxZoom:12,
        id: 'structure_map',
        opacity: 1,
        zIndex: 2,
        tileSize:mapSize,
        bounds:map.getBounds()
    });
    l.addTo(map.map);
}

//ANDROID
map.cmds = [
    map.addDinos,
    map.doGoToPos
]

map.onCmd = function(opcode, data) {
    var cmd = JSON.parse(atob(data));

    //Based on the command type, execute
    map.cmds[opcode](cmd);
}

//Call init. First, get settings
var settings = JSON.parse(atob(window.location.hash.substr(1)));
map.init(settings);