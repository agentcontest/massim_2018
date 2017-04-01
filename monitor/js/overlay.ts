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
  ].concat(dynamic.teams.map(team =>
    h('div', [h('strong', ['Team ', h('span.team.' + ctrl.normalizeTeam(team.name), team.name), ':']), ' ', n(team.money, '$')])
  )));
}

function details(ctrl: Ctrl, staticWorld: StaticWorld) {
  const sel = ctrl.selection();

  if (!sel) return h('div', [
    h('strong', 'Details:'), ' select an agent or facility'
  ])
  else if (isAgent(sel)) {
    const role = staticWorld.roles.filter(r => r.name === sel.role)[0];
    const lastAction = sel.lastAction || {
      type: 'noAction',
      result: 'successful'
    };
    return h('div', [
      h('div', h('strong',  ['Agent ', h('em', sel.name)])),
      h('div', ['charge: ', h('em', n(sel.charge))].concat(role ? [' / ', n(role.battery)] : [])),
      h('div', ['load: ', h('em', n(sel.load))]),
      h('div', ['lastAction: ', h('em', [lastAction.type, '(', ') = ', lastAction.result])])
    ]);
  }
  else return h('div', [
    h('div', h('strong', 'Facility:'))
  ].concat(Object.keys(sel).map(key =>
    h('div', [key, ': ', h('em', (sel as any)[key].toString())])
  )));
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
    h('div.btn', details(ctrl, ctrl.vm.static)),
    h('div.btn', jobs(ctrl.vm.dynamic))
  ]);
}
