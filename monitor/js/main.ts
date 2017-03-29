/// <reference path="../dts/openlayers.d.ts" />

import { init } from 'snabbdom';
import { VNode } from 'snabbdom/vnode';

import klass from 'snabbdom/modules/class';
import props from 'snabbdom/modules/props';
import attributes from 'snabbdom/modules/attributes';
import listeners from 'snabbdom/modules/eventlisteners';

import view from './view';
import makeCtrl from './ctrl';
import { Ctrl } from './interfaces';
import ol = require('openlayers');

const patch = init([klass, props, attributes, listeners]);

const CLAUSTHAL: ol.Coordinate = [10.340707, 51.8080063];

export default function Monitor(mapElement: Element, overlayElement: Element) {
  let vnode: VNode | Element = mapElement;
  let ctrl: Ctrl;

  function redraw() {
    vnode = patch(vnode, view(ctrl));
  }

  ctrl = makeCtrl(redraw);
  //patch(element, view(ctrl));

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

  /* const map = */ new ol.Map({
    target: mapElement,
    layers: [openStreetMapLayer, vectorLayer],
    view: new ol.View({
      center: ol.proj.fromLonLat(CLAUSTHAL),
      zoom: 15
    })
  });

  ctrl.connect();
}
