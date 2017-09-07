/// <reference path="../dts/openlayers.d.ts" />

import { Ctrl, MapView } from './interfaces';

import { init } from 'snabbdom';
import { VNode } from 'snabbdom/vnode';
import klass from 'snabbdom/modules/class';
import props from 'snabbdom/modules/props';
import attributes from 'snabbdom/modules/attributes';
import listeners from 'snabbdom/modules/eventlisteners';

import makeCtrl from './ctrl';
import makeMap from './map';
import overlay from './overlay';

const patch = init([klass, props, attributes, listeners]);

export default function Monitor(mapTarget: Element, overlayTarget: Element) {
  let vnode: VNode | Element = overlayTarget;
  let ctrl: Ctrl;
  let map: MapView;

  let redrawRequested = false;

  const redraw = function() {
    if (redrawRequested) return;
    redrawRequested = true;
    requestAnimationFrame(() => {
      redrawRequested = false;
      vnode = patch(vnode, overlay(ctrl));
      map.redraw();
    });
  };

  const hashChange = function() {
    if (ctrl.replay) {
      const step = parseInt(document.location.hash.substr(1), 10);
      if (step > 0) ctrl.replay.setStep(step);
      else if (!document.location.hash) ctrl.replay.start();
    }
  };

  const replayPath = window.location.search.length > 1 ?
    window.location.search.substr(1) : undefined;

  ctrl = makeCtrl(redraw, replayPath);

  hashChange();
  if ('onhashchange' in window) window.onhashchange = hashChange;

  map = makeMap(mapTarget, ctrl);
  redraw();
}
