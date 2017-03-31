import { Ctrl, StaticWorld, DynamicWorld } from './interfaces';

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

function simulation(staticWorld: StaticWorld, dynamic: DynamicWorld) {
  return h('div', [
    h('div', [h('strong', 'Simulation:'), ' ', staticWorld.simId]),
    h('div', [h('strong', 'Step:'), ' ', 0, ' / ', staticWorld.steps])
  ].concat(staticWorld.teams.map(team => {
    console.log(team);
    return h('div', [h('strong', 'Team ' + team + ':'), ' ', 0]);
  })));
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
  else if (ctrl.vm.state === 'connecting' || !ctrl.vm.static || !ctrl.vm.dynamic)
    return loading();
  else return h('div#overlay', [
    h('div.btn', simulation(ctrl.vm.static, ctrl.vm.dynamic)),
    h('div.btn', details()),
    h('div.btn', jobs())
  ]);
}
