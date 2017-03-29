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

export type FacilityType = 'workshop' | 'resourceNode' | 'shop' | 'dump' | 'chargingStation';

export interface Workshop {
  lat: number;
  lon: number;
  name: string;
}

export interface Dump {
  lat: number;
  lon: number;
}

export interface ResourceNode {
  lat: number;
  lon: number;
}

export interface Shop {
  lat: number;
  lon: number;
}

export interface ChargingStation {
  lat: number;
  lon: number;
}

export interface DynamicWorld {
  workshops: Workshop[];
  dumps: Dump[];
  resourceNodes: ResourceNode[];
  shops: Shop[];
  chargingStations: ChargingStation[];
}

export interface Located {
  lat: number;
  lon: number;
}

export type Redraw = () => void;
