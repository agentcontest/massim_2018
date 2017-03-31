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

function simulation() {
  return h('div', [
    h('strong', 'Simulation')
  ])
}

function details() {
  return h('div', [
    h('strong', 'Details:'), ' select an agent or facility'
  ])
}

function jobs() {
  return h('div', [
    h('strong', 'Jobs and auctions')
  ])
}

export default function(ctrl: Ctrl) {
  if (ctrl.vm.state === 'error') return disconnected();
  else if (ctrl.vm.state === 'connecting') return loading();
  else return h('div#overlay', [
    h('div.btn', simulation()),
    h('div.btn', details()),
    h('div.btn', jobs())
  ]);
}
