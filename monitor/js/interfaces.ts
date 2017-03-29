type ConnectionState = 'offline' | 'online' | 'connecting' | 'error';

export interface Ctrl {
  connect(): void;
  vm: ViewModel;
}

export interface MapView {
  redraw(): void;
}

export interface ViewModel {
  state: ConnectionState;
  dynamic?: DynamicWorld;
}

export type FacilityType = 'workshop';

export interface Workshop {
  lat: number;
  lon: number;
  name: string;
}

export interface DynamicWorld {
  workshops: Workshop[];
}

export interface Located {
  lat: number;
  lon: number;
}

export type Redraw = () => void;
