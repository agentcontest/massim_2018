import { init } from 'snabbdom';
import { VNode } from 'snabbdom/vnode';

import klass from 'snabbdom/modules/class';
import props from 'snabbdom/modules/props';
import attributes from 'snabbdom/modules/attributes';
import listeners from 'snabbdom/modules/eventlisteners';

import view from './view';
import makeCtrl from './ctrl';
import { Ctrl } from './interfaces';

const patch = init([klass, props, attributes, listeners]);

export default function Monitor(element: Element) {
  let vnode: VNode | Element = element;
  let ctrl: Ctrl;

  function redraw() {
    vnode = patch(vnode, view(ctrl));
  }

  ctrl = makeCtrl(redraw);

  patch(element, view(ctrl));
}
