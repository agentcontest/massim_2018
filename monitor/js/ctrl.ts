import { Redraw, Ctrl } from './interfaces';

export default function(redraw: Redraw): Ctrl {
  const connect = function() {
    const ws = new WebSocket('ws://' + document.location.host + '/socket');

    ws.onmessage = function(msg) {
      let data = JSON.parse(msg.data);
      console.log(data);
    };

    ws.onopen = function() {
      console.log('Connected');
    };

    ws.onclose = function(event) {
      console.log('Disconnected', event);
      setTimeout(connect, 5000);
    };
  };

  return {
    connect: connect
  };
}
