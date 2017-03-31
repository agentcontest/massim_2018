import { Ctrl, MapView, Located, Facility, FacilityType, Agent } from './interfaces';

import ol = require('openlayers');

const CLAUSTHAL: ol.Coordinate = [10.340707, 51.8080063];

function xy(lonlat: Located): ol.Coordinate {
  return ol.proj.fromLonLat([lonlat.lon, lonlat.lat]);
}

function facilityStyle(type: FacilityType, selected: boolean): ol.style.Style {
  const suffix = selected ? '-h' : '';

  return new ol.style.Style({
    image: new ol.style.Icon({
      src: '/img/' + type + suffix + '.png',
      anchor: [17, 50],
      anchorXUnits: 'pixels',
      anchorYUnits: 'pixels'
    })
  });
}

export default function(target: Element, ctrl: Ctrl): MapView {
  let simId: string | undefined;

  const vectorSource = new ol.source.Vector();

  const agentIconStyle = function(entity: Agent, selected: boolean, active?: boolean): ol.style.Style {
    let suffix = '';
    if (selected) suffix = '-h';
    else if (!active) suffix = '-i';

    return new ol.style.Style({
      image: new ol.style.Icon({
        src: '/img/' + entity.role + '-' + ctrl.normalizeTeam(entity.team) + suffix + '.png',
        anchor: [25, 25],
        anchorXUnits: 'pixels',
        anchorYUnits: 'pixels'
      })
    });
  };

  const openStreetMapLayer = new ol.layer.Tile({
    source: new ol.source.OSM({
      attributions: [ol.source.OSM.ATTRIBUTION],
      url: 'http://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
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
    let found = false;
    map.forEachFeatureAtPixel(map.getEventPixel(e), feature => {
      if (!found) {
        const userData = (feature as any).userData;
        if (userData && userData.name) ctrl.setSelection(userData.name);
        found = true;
      }
    });
    if (!found) ctrl.setSelection(null);
  });

  const redraw = function() {
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
        addFeature(facility, facilityStyle(type, facility.name === ctrl.vm.selected));
      };
    };

    ctrl.vm.dynamic.workshops.forEach(renderFacility('workshop'));
    ctrl.vm.dynamic.dumps.forEach(renderFacility('dump'));
    ctrl.vm.dynamic.shops.forEach(renderFacility('shop'));
    ctrl.vm.dynamic.chargingStations.forEach(renderFacility('chargingStation'));
    ctrl.vm.dynamic.resourceNodes.forEach(renderFacility('resourceNode'));
    ctrl.vm.dynamic.storages.forEach(renderFacility('storage'));

    ctrl.vm.dynamic.entities.forEach(agent => {
      const active = agent.lastAction && agent.lastAction.type !== 'noAction' &&
                     agent.lastAction.result.indexOf('successful') === 0;
      addFeature(agent, agentIconStyle(agent, agent.name === ctrl.vm.selected, active));
    });

    if (simId !== ctrl.vm.static.simId) {
      // adjust the map when a new simulation starts
      map.getView().fit(vectorSource.getExtent(), map.getSize());
      simId = ctrl.vm.static.simId;
    }
  };

  return {
    redraw: redraw
  };
}
