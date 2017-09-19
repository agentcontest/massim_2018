import { Ctrl, ReplayCtrl, StaticWorld, DynamicWorld, Shop, Storage, isAgent } from './interfaces';

import { h } from 'snabbdom';
import { VNode } from 'snabbdom/vnode';

const MAX_JOBS = 12;

function n(num: number, suffix?: string): string {
  const s = num.toString().replace(/\B(?=(\d{3})+(?!\d))/g, "\u200a");
  return suffix ? (s + "\u200a" + suffix) : s;
}

function loading() {
  return h('div.modal-overlay', h('div.loader', 'Loading ...'));
}

function disconnected(ctrl: Ctrl) {
  return h('div.modal-overlay', [
    h('p', ctrl.replay ? 'Replay unavailable.' : 'Live server not connected.'),
    h('a', {
      props: { href: document.location.pathname + document.location.search }
    }, 'Retry now.')
  ]);
}

function replay(ctrl: ReplayCtrl) {
  return h('div.btn.replay', [
    h('div', [h('strong', 'Replay:'), ' ', ctrl.name()]),
    h('div', [
      h('button', { on: { click: () => ctrl.setStep(0) } }, '|<<'),
      h('button', { on: { click: () => ctrl.setStep(ctrl.step() - 10) } }, '<<'),
      h('button', {
        on: { click: () => ctrl.toggle() }
      }, ctrl.playing() ? '||' : '>'),
      h('button', { on: { click: () => ctrl.setStep(ctrl.step() + 10) } }, '>>'),
      h('button', { on: { click: () => ctrl.setStep(99999999) } }, '>>|')
    ])
  ]);
}

function simulation(ctrl: Ctrl, staticWorld: StaticWorld, dynamic: DynamicWorld) {
  return h('div', [
    h('div', [h('strong', 'Simulation:'), ' ', staticWorld.simId]),
    h('div', [h('strong', 'Step:'), ' ', n(dynamic.step), ' / ', n(staticWorld.steps - 1)])
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
      result: 'successful',
      params: []
    };
    return h('div', [
      h('div', h('strong',  ['Agent ', h('em', sel.name)])),
      h('div', ['charge: ', h('em', n(sel.charge))].concat(role ? [' / ', n(role.battery)] : [])),
      h('div', ['load: ', h('em', n(sel.load))]),
      h('div', ['lastAction: ', h('em', [lastAction.type, '(', lastAction.params.join(', '), ') = ', lastAction.result])])
    ]);
  }
  else return h('div', [
    h('div', h('strong', 'Facility:'))
  ].concat(Object.keys(sel).map(key => {
    if (key == 'storedItems') return h('span');
    else if (key == 'offeredItems') {
      return h('div', ['items: ', h('ul', (sel as Shop).offeredItems.map(stock =>
        h('li', n(stock.amount, 'x') + ' ' + stock.name + ' @ ' + n(stock.price, '$'))
      ))]);
    }
    else if (key == 'allStoredItems') {
      return h('div', ['items: ', storageItems(ctrl, sel as Storage)]);
    }
    else {
      return h('div', [key, ': ', h('em', (sel as any)[key].toString())])
    }
  })));
}

function storageItems(ctrl: Ctrl, storage: Storage): VNode {
  const ul = [];
  for (let data of storage.allStoredItems) {
    for (let item of data.stored) {
       if (item.stored > 0) ul.push(h('li', [
         'Team ', h('span.team.' + ctrl.normalizeTeam(data.teamName), data.teamName), ': ',
         n(item.stored, 'x') + ' ' + item.name
       ]));

       if (item.delivered > 0) ul.push(h('li', [
         'Team ', h('span.team.' + ctrl.normalizeTeam(data.teamName), data.teamName), ': ',
         n(item.delivered, 'x') + ' ' + item.name + ' (delivered)'
       ]));
    }
  }
  return ul.length ? h('ul', ul) : h('em', 'none');
}

function jobs(dynamic: DynamicWorld) {
  const jobs = dynamic.jobs.filter((j) => j.end <= dynamic.step);

  return h('div', [
    h('strong', 'Jobs and auctions'),
    h('ul', jobs.slice(0, MAX_JOBS).map(job => {
      return h('li', [
        h('em', n(job.reward, '$')), ' by ', h('em', job.poster)
      ])
    })),
    jobs.length > MAX_JOBS ? h('div', 'and ' + (jobs.length - MAX_JOBS) + ' more ongoing') : null
  ]);
}

export default function(ctrl: Ctrl) {
  if (ctrl.vm.state === 'error') return disconnected(ctrl);
  if (ctrl.vm.state === 'connecting' || !ctrl.vm.static || !ctrl.vm.dynamic)
    return h('div', [
      loading(),
      ctrl.replay ? h('div#overlay.replay', replay(ctrl.replay)) : undefined
    ]);
  else return h('div#overlay', [
    ctrl.replay ? replay(ctrl.replay) : undefined,
    h('div.btn', simulation(ctrl, ctrl.vm.static, ctrl.vm.dynamic)),
    h('div.btn', details(ctrl, ctrl.vm.static)),
    h('div.btn', jobs(ctrl.vm.dynamic))
  ]);
}
