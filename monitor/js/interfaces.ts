type ConnectionState = 'offline' | 'online' | 'connecting' | 'error';

export interface Ctrl {
  toggleSelection(name: string): void;
  connect(): void;
  vm: ViewModel;
}

export interface MapView {
  redraw(): void;
}

export interface ViewModel {
  state: ConnectionState;
  selected: string | null;
  dynamic?: DynamicWorld;
}

export type FacilityType = 'workshop' | 'resourceNode' | 'shop' | 'dump' |
                           'chargingStation' | 'storage';

export interface Workshop {
  lat: number;
  lon: number;
  name: string;
}

export interface Dump {
  lat: number;
  lon: number;
}

export interface Storage {
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

export interface Facility {
  lat: number;
  lon: number;
  name: string;
}

export type Role = 'SpaceShip';

export interface Agent {
  lat: number;
  lon: number;
  role: Role;
  team: string;
  name: string;
}

export interface DynamicWorld {
  workshops: Workshop[];
  dumps: Dump[];
  resourceNodes: ResourceNode[];
  shops: Shop[];
  chargingStations: ChargingStation[];
  storages: Storage[];
  entities: Agent[];
}

export interface Located {
  lat: number;
  lon: number;
}

export type Redraw = () => void;
