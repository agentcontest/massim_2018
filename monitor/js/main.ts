import { h, init } from 'snabbdom';
//import { VNode } from 'snabbdom/vnode';

import klass from 'snabbdom/modules/class';
import props from 'snabbdom/modules/props';
import attributes from 'snabbdom/modules/attributes';
import listeners from 'snabbdom/modules/eventlisteners';

const patch = init([klass, props, attributes, listeners]);

function view() {
  return h('div', 'hello world');
}

export default function Monitor(element: Element) {
  patch(element, view());
}
