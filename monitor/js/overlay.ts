import { Ctrl, StaticWorld, DynamicWorld, isAgent } from './interfaces';

import { h } from 'snabbdom';

function n(num: number, suffix?: string): string {
  const s = num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, "\u200a");
  return suffix ? (s + "\u200a" + suffix) : s;
}

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

function simulation(ctrl: Ctrl, staticWorld: StaticWorld, dynamic: DynamicWorld) {
  return h('div', [
    h('div', [h('strong', 'Simulation:'), ' ', staticWorld.simId]),
    h('div', [h('strong', 'Step:'), ' ', n(dynamic.step), ' / ', n(staticWorld.steps)])
  ].concat(staticWorld.teams.map(team =>
    h('div', [h('strong', ['Team ', h('span.team.' + ctrl.normalizeTeam(team), team), ':']), ' ', n(0, '$')])
  )));
}

function details(ctrl: Ctrl) {
  const selection = ctrl.selection();

  if (selection) {
    return h('div', [
      h('div', h('strong', isAgent(selection) ? 'Agent:' : 'Facility:'))
    ].concat(Object.keys(selection).map(key =>
      h('div', [key, ': ', h('em', (selection as any)[key].toString())])
    )));
  }
  else return h('div', [
    h('strong', 'Details:'), ' select an agent or facility'
  ]);
}

function jobs(dynamic: DynamicWorld) {
  return h('div', [
    h('strong', 'Jobs and auctions'),
    h('ul', dynamic.jobs.map(job => {
      return h('li', [
        h('em', n(job.reward, '$')), ' by ', h('em', job.poster)
      ])
    }))
  ]);
}

export default function(ctrl: Ctrl) {
  if (ctrl.vm.state === 'error') return disconnected();
  else if (ctrl.vm.state === 'connecting' || !ctrl.vm.static || !ctrl.vm.dynamic)
    return loading();
  else return h('div#overlay', [
    h('div.btn', simulation(ctrl, ctrl.vm.static, ctrl.vm.dynamic)),
    h('div.btn', details(ctrl)),
    h('div.btn', jobs(ctrl.vm.dynamic))
  ]);
}
