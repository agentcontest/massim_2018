import { Redraw, Ctrl, ViewModel, Agent, Facility } from './interfaces';

const TEAMS = ['a', 'b'];

export default function(redraw: Redraw): Ctrl {
  const vm: ViewModel = {
    state: 'connecting',
    selected: [],
    selectionIndex: 0,
  };

  const connect = function() {
    const ws = new WebSocket('ws://' + document.location.host + '/socket');

    ws.onmessage = function(msg) {
      const data = JSON.parse(msg.data);
      console.log(data);
      if (data.simId) vm.static = data;
      else vm.dynamic = data;
      redraw();
    };

    ws.onopen = function() {
      console.log('Connected');
      vm.state = 'online';
      redraw();
    };

    ws.onclose = function() {
      console.log('Disconnected');
      setTimeout(connect, 5000);
      vm.state = 'error';
      redraw();
    };
  };

  const entities = function(): Array<Agent | Facility> {
    const d = vm.dynamic;
    if (!d) return [];
    return ([] as Array<Agent | Facility>).concat(
      d.entities,
      d.workshops,
      d.dumps,
      d.resourceNodes,
      d.shops,
      d.chargingStations,
      d.storages
    );
  };

  const selectionChanged = function(names: string[]): boolean {
    if (vm.selected.length !== names.length) return true;
    for (var i = 0; i < names.length; i++) {
      if (vm.selected.indexOf(names[i]) === -1) return true;
    }
    return false;
  };

  return {
    connect: connect,
    vm: vm,
    entities: entities,
    setSelection(names: string[]) {
      if (selectionChanged(names)) {
        vm.selected = names;
        vm.selectionIndex = 0;
      } else {
        // cycle selection
        vm.selectionIndex = (vm.selectionIndex + 1) % vm.selected.length;
      }
      redraw();
    },
    selection: () => {
      if (!vm.selected.length) return null;
      return entities().filter(entity => entity.name === vm.selected[vm.selectionIndex])[0];
    },
    normalizeTeam(team: string) {
      if (vm.static) return TEAMS[vm.static.teams.indexOf(team)] || 'a';
      else return 'a';
    }
  };
}
