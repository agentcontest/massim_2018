import { Ctrl, MapView, Located, Facility, FacilityType, Agent } from './interfaces';

import { h } from 'snabbdom';
import ol = require('openlayers');

function loading() {
  return h('div.modal-overlay', h('div.loader', 'Loading ...'));
}

function disconnected() {
  return h('div.modal-overlay', [
    h('p', 'Not connected to the monitor.'),
    h('a', {
      props: { href: '/' }
    }, 'Retry now.')
  ]);
}

//const CLAUSTHAL: ol.Coordinate = [10.340707, 51.8080063];
const LONDON: ol.Coordinate = [-0.1257400, 51.5085300];

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

function log<T>(s: T): T {
  console.log(s);
  return s;
}

export function makeMap(target: Element, ctrl: Ctrl): MapView {

  const vectorSource = new ol.source.Vector();

  const agentIconStyle = function(entity: Agent, selected: boolean, active?: boolean): ol.style.Style {
    let suffix = '';
    if (selected) suffix = '-h';
    else if (!active) suffix = '-i';

    const team = entity.team.toLowerCase();

    return new ol.style.Style({
      image: new ol.style.Icon({
        src: '/img/' + entity.role + '-' + team + suffix + '.png',
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
      center: ol.proj.fromLonLat(LONDON),
      zoom: 13
    })
  });

  map.getViewport().addEventListener('click', e => {
    let first = true;
    map.forEachFeatureAtPixel(map.getEventPixel(e), feature => {
      if (first) {
        const userData = (feature as any).userData;
        if (userData && userData.name) ctrl.toggleSelection(userData.name);
        first = false;
      }
    });
  });

  const redraw = function() {
    vectorSource.clear();
    if (!ctrl.vm.dynamic) return;


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
  };

  return {
    redraw: redraw
  };
}

export function overlay(ctrl: Ctrl) {
  log(ctrl.vm.state);
  if (ctrl.vm.state === 'error') return disconnected();
  else if (ctrl.vm.state === 'connecting') return loading();
  else return h('div', 'yay :)');
}
