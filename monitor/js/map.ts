import { Ctrl, MapView, Located, Facility, FacilityType, Agent, Well } from './interfaces';

import ol = require('openlayers');

const CLAUSTHAL: ol.Coordinate = [10.340707, 51.8080063];
const COLORS: { [index:string] : string } = {a: 'rgba(0,114,178,1)', b: 'rgba(0,158,115,1)', c: 'rgba(98,98,98,1)'};

function xy(lonlat: Located): ol.Coordinate {
  return ol.proj.fromLonLat([lonlat.lon, lonlat.lat]);
}

function facilityStyle(type: FacilityType, selected: boolean): ol.style.Style {
  const suffix = selected ? '-h' : '';

  return new ol.style.Style({
    image: new ol.style.Icon({
      src: 'img/' + type + suffix + '.png',
      anchor: [17, 50],
      anchorXUnits: 'pixels',
      anchorYUnits: 'pixels'
    })
  });
}

export default function(target: Element, ctrl: Ctrl): MapView {
  let currentMap: string | undefined;

  const vectorSource = new ol.source.Vector();

  const agentIconStyle = function(entity: Agent, selected: boolean, active?: boolean): ol.style.Style {
    let suffix = '';
    if (selected) suffix = '-h';
    else if (!active) suffix = '-i';

    return new ol.style.Style({
      image: new ol.style.Icon({
        src: 'img/' + entity.role + '-' + ctrl.normalizeTeam(entity.team) + suffix + '.png',
        anchor: [25, 25],
        anchorXUnits: 'pixels',
        anchorYUnits: 'pixels'
      })
    });
  };

  const teamColorStyle = function(agent: Agent): ol.style.Style {
    return new ol.style.Style({
      stroke: new ol.style.Stroke({
        color: COLORS[ctrl.normalizeTeam(agent.team)] || COLORS.a,
        width: 3
      }),
    });
  };

  const wellIconStyle = function(well: Well, selected: boolean): ol.style.Style {
    const suffix = selected ? '-h' : '';
    return new ol.style.Style({
      image: new ol.style.Icon({
        src: 'img/well-' + ctrl.normalizeTeam(well.team) + suffix + '.png',
        anchor: [27, 67],
        anchorXUnits: 'pixels',
        anchorYUnits: 'pixels'
      })
    });
  };

  const openStreetMapLayer = new ol.layer.Tile({
    source: new ol.source.OSM({
      attributions: [ol.source.OSM.ATTRIBUTION],
      url: '//{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
    })
  });

  const vectorLayer = new ol.layer.Vector({
    source: vectorSource
  });

  const map = new ol.Map({
    target: target,
    layers: [openStreetMapLayer, vectorLayer],
    view: new ol.View({
      center: ol.proj.fromLonLat(CLAUSTHAL),
      zoom: 13
    })
  });

  map.getViewport().addEventListener('click', e => {
    // Find entities under the cursor.
    const underCursor: string[] = [];
    map.forEachFeatureAtPixel(map.getEventPixel(e), feature => {
      const userData = (feature as any).userData;
      if (userData && userData.name) underCursor.push(userData.name);
    });
    ctrl.setSelection(underCursor);
  });

  const redraw = function() {
    const view = map.getView();

    vectorSource.clear();
    if (!ctrl.vm.static || !ctrl.vm.dynamic) return;

    const addFeature = function(loc: Located, style: ol.style.Style) {
      const feature = new ol.Feature({
        geometry: new ol.geom.Point(xy(loc))
      });

      feature.setStyle(style);
      (feature as any).userData = loc;

      vectorSource.addFeature(feature);
    };

    const renderFacility = function(type: FacilityType) {
      return function(facility: Facility) {
        addFeature(facility, facilityStyle(type, facility === ctrl.selection()));
      };
    };

    ctrl.vm.dynamic.workshops.forEach(renderFacility('workshop'));
    ctrl.vm.dynamic.dumps.forEach(renderFacility('dump'));
    ctrl.vm.dynamic.shops.forEach(renderFacility('shop'));
    ctrl.vm.dynamic.chargingStations.forEach(renderFacility('chargingStation'));
    ctrl.vm.dynamic.resourceNodes.forEach(renderFacility('resourceNode'));
    ctrl.vm.dynamic.storages.forEach(renderFacility('storage'));
    ctrl.vm.dynamic.wells.forEach(well => addFeature(well, wellIconStyle(well, well === ctrl.selection())));

    const renderRoute = function(agent: Agent) {
      const polyline = new ol.geom.LineString([xy(agent)].concat(agent.route.map(xy)));
      const feature = new ol.Feature({ geometry: polyline });
      feature.setStyle(teamColorStyle(agent));
      vectorSource.addFeature(feature);
    };

    const renderAgent = function(agent: Agent) {
      const active = agent.lastAction && agent.lastAction.type !== 'noAction' &&
                     agent.lastAction.result.indexOf('successful') === 0;
      addFeature(agent, agentIconStyle(agent, agent === ctrl.selection(), active));
      renderRoute(agent);
    };

    ctrl.vm.dynamic.entities.forEach(agent => {
      if (agent !== ctrl.selection()) renderAgent(agent);
    });
    ctrl.vm.dynamic.entities.forEach(agent => {
      if (agent === ctrl.selection()) {
        renderAgent(agent);

        // draw vision radius as a circle
        const center = ol.proj.fromLonLat([agent.lon, agent.lat]);
        const resolutionFactor = view.getResolution() / ol.proj.getPointResolution(view.getProjection(), view.getResolution(), center);
        const radius = (agent.vision / ol.proj.METERS_PER_UNIT.m) * resolutionFactor;
        const circle = new ol.geom.Circle(center, radius);
        vectorSource.addFeature(new ol.Feature(circle));
      }
    });

    // draw rectangle with the extents of the map
    const extent = new ol.geom.LineString([
      ol.proj.fromLonLat([ctrl.vm.static.minLon, ctrl.vm.static.minLat]),
      ol.proj.fromLonLat([ctrl.vm.static.minLon, ctrl.vm.static.maxLat]),
      ol.proj.fromLonLat([ctrl.vm.static.maxLon, ctrl.vm.static.maxLat]),
      ol.proj.fromLonLat([ctrl.vm.static.maxLon, ctrl.vm.static.minLat]),
      ol.proj.fromLonLat([ctrl.vm.static.minLon, ctrl.vm.static.minLat])
    ]);
    vectorSource.addFeature(new ol.Feature({ geometry: extent }));

    if (currentMap !== ctrl.vm.static.map) {
      // adjust the map when a new simulation starts
      view.fit(extent, {
        size: map.getSize(),
        padding: [50, 50, 50, 50],
        constrainResolution: false
      });
      currentMap = ctrl.vm.static.map;
    }
  };

  return {
    redraw: redraw
  };
}
