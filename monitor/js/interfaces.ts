type ConnectionState = 'offline' | 'online' | 'connecting' | 'error';

export interface Ctrl {
  entities(): Array<Agent | Facility>;
  setSelection(names: string[]): void;
  selection(): Agent | Facility | null;
  normalizeTeam(team: string): string;
  vm: ViewModel;
  replay?: ReplayCtrl,
}

export interface MapView {
  redraw(): void;
}

export interface ViewModel {
  state: ConnectionState;
  selected: string[];
  selectionIndex: number;
  dynamic?: DynamicWorld;
  static?: StaticWorld;
}

export interface ReplayCtrl {
  name(): string;
  step(): number;
  setStep(s: number): void;
  toggle(): void;
  stop(): void;
  start(): void;
  playing(): boolean;
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
  usedCapacity: number;
  totalCapacity: number;
  allStoredItems: StorageData[];
}

export interface StorageData {
  teamName: string;
  stored: StorageItem[];
}

export interface StorageItem {
  name: string;
  delivered: number;
  stored: number;
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
  offeredItems: StockData[];
  restock: number;
}

export interface StockData {
  amount: number;
  name: string;
  price: number;
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

export interface Well {
  lat: number;
  lon: number;
  name: string;
  team: string;
  type: string;
  integrity: number;
}

export type RoleName = 'SpaceShip';

export interface Agent {
  lat: number;
  lon: number;
  name: string;
  role: RoleName;
  team: string;
  load: number;
  loadMax: number;
  charge: number;
  chargeMax: number;
  skill: number;
  speed: number;
  vision: number;
  routeLength: number;
  route: Waypoint[];
  items: ItemAmount[];
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
  params: string[];
}

export interface ItemAmount {
  name: string;
  amount: number;
}

export interface Job {
  id: string;
  poster: string;
  start: number;
  end: number;
  requiredItems: ItemAmount[];
  deliveredItems: ItemAmount[];
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
  wells: Well[];
  jobs: Job[];
  teams: Team[];
}

export interface Located {
  lat: number;
  lon: number;
}

export interface Team {
  name: string;
  score: number;
  massium: number;
}

export interface Role {
  name: RoleName;
  baseBattery: number;
  baseLoad: number;
  baseSkill: number;
  baseSpeed: number;
  baseVision: number;
  maxBattery: number;
  maxLoad: number;
  maxSkill: number;
  maxSpeed: number;
  maxVision: number;
  tools: string[];
}

export interface StaticWorld {
  simId: string;
  teams: string[];
  roles: Role[];
  steps: number;
  map: string;
  minLat: number;
  maxLat: number;
  minLon: number;
  maxLon: number;
}

export interface Waypoint {
  index: number,
  lat: number,
  lon: number
}

export type Redraw = () => void;
