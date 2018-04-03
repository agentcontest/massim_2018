import { Redraw, Ctrl, ReplayCtrl, ViewModel, Agent, Well, Facility } from './interfaces';

const TEAMS = ['a', 'b', 'c'];

export default function(redraw: Redraw, replayPath?: string): Ctrl {
  const vm: ViewModel = {
    state: 'connecting',
    selected: [],
    selectionIndex: 0,
  };

  const connect = function() {
    const protocol = document.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const ws = new WebSocket(protocol + '//' + document.location.host + '/socket');

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

  const makeReplayCtrl = function(path: string): ReplayCtrl {
    if (path[path.length - 1] == '/') path = path.substr(0, path.length - 1);

    var step = 0;
    var timer: number | undefined = undefined;

    var cache: any = {};
    var cacheSize = 0;

    function stop() {
      if (timer) clearInterval(timer);
      timer = undefined;
      redraw();
    }

    function start() {
      if (!timer) timer = setInterval(function() {
        if (vm.state !== 'connecting') setStep(step + 1);
      }, 1000);
      redraw();
    }

    function loadStatic() {
      const xhr = new XMLHttpRequest();
      xhr.open('GET', path + '/static.json');
      xhr.onload = function() {
        if (xhr.status === 200) {
          vm.static = JSON.parse(xhr.responseText);
          setStep(step);
        } else {
          vm.state = 'error';
        }
        redraw();
      };
      xhr.onerror = function() {
        vm.state = 'error';
        redraw();
      };
      xhr.send();
    }

    function loadDynamic(step: number) {
      // got from cache
      if (cache[step]) {
        vm.dynamic = cache[step];
        vm.state = (vm.dynamic && vm.dynamic.step == step) ? 'online' : 'connecting';
        redraw();
        return;
      }

      const group = Math.floor(step / 5) * 5;
      const xhr = new XMLHttpRequest();
      xhr.open('GET', path + '/' + group + '.json');
      xhr.onload = function() {
        if (xhr.status === 200) {
          var response = JSON.parse(xhr.responseText);
          vm.dynamic = response[step];
          vm.state = (vm.dynamic && vm.dynamic.step == step) ? 'online' : 'connecting';

          // write to cache
          if (cacheSize > 100) {
            cache = {};
            cacheSize = 0;
          }
          for (var s in response) {
            cache[s] = response[s];
            cacheSize++;
          }
        } else {
          vm.state = 'error';
          stop();
        }
        redraw();
      };
      xhr.onerror = function() {
        vm.state = 'error';
        stop();
        redraw();
      };
      xhr.send();
    }

    function setStep(s: number) {
      // keep step in bounds
      step = Math.max(0, s);
      if (vm.static && step >= vm.static.steps) {
        stop();
        step = vm.static.steps - 1;
      }

      // show connecting after a while
      vm.state = 'connecting';
      setTimeout(() => redraw(), 500);

      // update url
      if (history.replaceState) history.replaceState({}, document.title, '#' + step);

      loadDynamic(step);
    }

    loadStatic();

    return {
      name: function() {
        const parts = path.split('/');
        return parts[parts.length - 1];
      },
      step: function() {
        return step;
      },
      setStep,
      toggle: function() {
        if (timer) stop();
        else start();
      },
      stop,
      start,
      playing: function() {
        return !!timer;
      }
    };
  };

  const replay = replayPath ? makeReplayCtrl(replayPath) : undefined;
  if (!replay) connect();

  const entities = function(): Array<Agent | Facility> {
    const d = vm.dynamic;
    if (!d) return [];
    return ([] as Array<Agent | Facility | Well>).concat(
      d.entities,
      d.workshops,
      d.dumps,
      d.resourceNodes,
      d.shops,
      d.chargingStations,
      d.storages,
      d.wells
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
    replay: replay,
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
    selection() {
      if (!vm.selected.length) return null;
      return entities().filter(entity => entity.name === vm.selected[vm.selectionIndex])[0];
    },
    normalizeTeam(team: string) {
      if (vm.static) return TEAMS[vm.static.teams.indexOf(team) % TEAMS.length] || 'a';
      else return 'a';
    }
  };
}
