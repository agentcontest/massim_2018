/// <reference path="../dts/openlayers.d.ts" />

import { Ctrl, MapView } from './interfaces';

import { init } from 'snabbdom';
import { VNode } from 'snabbdom/vnode';
import klass from 'snabbdom/modules/class';
import props from 'snabbdom/modules/props';
import attributes from 'snabbdom/modules/attributes';
import listeners from 'snabbdom/modules/eventlisteners';

import { makeMap, overlay } from './view';
import makeCtrl from './ctrl';

const patch = init([klass, props, attributes, listeners]);

export default function Monitor(mapTarget: Element, overlayTarget: Element) {
  let vnode: VNode | Element = overlayTarget;
  let ctrl: Ctrl;
  let map: MapView;

  const redraw = function() {
    vnode = patch(vnode, overlay(ctrl));
    map.redraw();
  };

  ctrl = makeCtrl(redraw);
  map = makeMap(mapTarget, ctrl);
  redraw();

  ctrl.connect();
}
