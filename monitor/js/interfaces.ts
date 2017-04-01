type ConnectionState = 'offline' | 'online' | 'connecting' | 'error';

export interface Ctrl {
  setSelection(name: string | null): void;
  selection(): Agent | Facility | null;
  normalizeTeam(team: string): string;
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
  static?: StaticWorld;
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
  name: string;
}

export interface Storage {
  lat: number;
  lon: number;
  name: string;
}

export interface ResourceNode {
  lat: number;
  lon: number;
  name: string;
}

export interface Shop {
  lat: number;
  lon: number;
  name: string;
}

export interface ChargingStation {
  lat: number;
  lon: number;
  name: string;
}

export interface Facility {
  lat: number;
  lon: number;
  name: string;
}

export type RoleName = 'SpaceShip';

export interface Agent {
  lat: number;
  lon: number;
  name: string;
  role: RoleName;
  team: string;
  load: number;
  charge: number;
  routeLength: number;
  route: any[];
  items: any[];
  facility?: string;
  lastAction?: LastAction;
}

export function isAgent(entity: Agent | Facility): entity is Agent {
  return !!(entity.name && (entity as Agent).role);
}

export type ActionResult = string;

export type ActionType = 'noAction' | string;

export interface LastAction {
  result: ActionResult;
  type: ActionType;
}

export interface RequiredItem {
  name: string;
  amount: number;
}

export interface Job {
  id: string;
  poster: string;
  start: number;
  end: number;
  requiredItems: RequiredItem[];
  deliveredItems: RequiredItem[];
  lowestBid: number;
  reward: number;
  storage: string;
  auctionTime: number;
}

export interface DynamicWorld {
  step: number;
  workshops: Workshop[];
  dumps: Dump[];
  resourceNodes: ResourceNode[];
  shops: Shop[];
  chargingStations: ChargingStation[];
  storages: Storage[];
  entities: Agent[];
  jobs: Job[];
  teams: Team[];
}

export interface Located {
  lat: number;
  lon: number;
}

export interface Team {
  name: string;
  money: number;
}

export interface Role {
  name: RoleName;
  battery: number;
  load: number;
  speed: number;
  tools: string[];
}

export interface StaticWorld {
  simId: string;
  teams: string[];
  roles: Role[];
  steps: number;
}

export type Redraw = () => void;
