type ConnectionState = 'offline' | 'online' | 'connecting' | 'error';

export interface Ctrl {
  connect(): void;
  vm: ViewModel;
}

export interface ViewModel {
  state: ConnectionState;
}

export type Redraw = () => void;
