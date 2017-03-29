import { Ctrl } from './interfaces';

import { h } from 'snabbdom';

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

export default function(ctrl: Ctrl) {
  console.log(ctrl.vm.state);
  if (ctrl.vm.state === 'error') return disconnected();
  else if (ctrl.vm.state === 'connecting') return loading();
  else return h('div', 'yay :)');
}
