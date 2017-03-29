import { Redraw, Ctrl, ViewModel } from './interfaces';

export default function(redraw: Redraw): Ctrl {
  const vm: ViewModel = {
    state: 'connecting'
  };

  const connect = function() {
    const ws = new WebSocket('ws://' + document.location.host + '/socket');

    ws.onmessage = function(msg) {
      let data = JSON.parse(msg.data);
      console.log(data);
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
    vm: vm
  };
}
