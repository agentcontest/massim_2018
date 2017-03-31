import { Redraw, Ctrl, ViewModel } from './interfaces';

const TEAMS = ['a', 'b'];

export default function(redraw: Redraw): Ctrl {
  const vm: ViewModel = {
    state: 'connecting',
    selected: null
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

  return {
    connect: connect,
    vm: vm,
    setSelection(name: string | null) {
      vm.selected = name;
      redraw();
    },
    normalizeTeam(team: string) {
      if (vm.static) return TEAMS[vm.static.teams.indexOf(team)] || 'a';
      else return 'a';
    }
  };
}
