import { Ctrl, MapView, Located, FacilityType } from './interfaces';

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

const CLAUSTHAL: ol.Coordinate = [10.340707, 51.8080063];

function xy(lonlat: Located): ol.Coordinate {
  return ol.proj.fromLonLat([lonlat.lon, lonlat.lat]);
}

function facilityStyle(type: FacilityType, active: boolean) {
  const suffix = active ? '-h' : '';

  return new ol.style.Style({
    image: new ol.style.Icon({
      src: '/img/' + type + suffix + '.png',
      anchor: [17, 50],
      anchorXUnits: 'pixels',
      anchorYUnits: 'pixels'
    })
  });
}

export function makeMap(target: Element, ctrl: Ctrl): MapView {

  const vectorSource = new ol.source.Vector();

  const openStreetMapLayer = new ol.layer.Tile({
    source: new ol.source.OSM({
      attributions: [ol.source.OSM.ATTRIBUTION],
      url: 'http://{a-c}.tile.openstreetmap.org/{z}/{x}/{y}.png'
    })
  });

  const vectorLayer = new ol.layer.Vector({
    source: vectorSource
  });

  new ol.Map({
    target: target,
    layers: [openStreetMapLayer, vectorLayer],
    view: new ol.View({
      center: ol.proj.fromLonLat(CLAUSTHAL),
      zoom: 15
    })
  });

  const redraw = function() {
    vectorSource.clear();
    if (!ctrl.vm.dynamic) return;

    ctrl.vm.dynamic.workshops.forEach(workshop => {
      const feature = new ol.Feature({
        geometry: new ol.geom.Point(xy(workshop))
      });

      feature.setStyle(facilityStyle('workshop', false));

      vectorSource.addFeature(feature);
    });
  };

  return {
    redraw: redraw
  };
}

export function overlay(ctrl: Ctrl) {
  console.log(ctrl.vm.state);
  if (ctrl.vm.state === 'error') return disconnected();
  else if (ctrl.vm.state === 'connecting') return loading();
  else return h('div', 'yay :)');
}
